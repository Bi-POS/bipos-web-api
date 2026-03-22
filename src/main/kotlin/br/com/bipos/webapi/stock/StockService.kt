package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.stock.*
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.exception.InsufficientStockException
import br.com.bipos.webapi.exception.ProductNotFoundException
import br.com.bipos.webapi.exception.StockNotFoundException
import br.com.bipos.webapi.sale.product.SaleProductRepository
import br.com.bipos.webapi.stock.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class StockService(
    private val stockRepository: StockRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val productExpiryBatchRepository: ProductExpiryBatchRepository,
    private val operationPointRepository: OperationPointRepository,
    private val consumptionRecordRepository: ConsumptionRecordRepository,
    private val productRepository: SaleProductRepository
) {

    // ============= OPERAÇÕES BÁSICAS DE ESTOQUE =============

    @Transactional
    fun initializeStock(
        productId: UUID,
        company: Company?,
        initialQuantity: BigDecimal,
        minimumQuantity: BigDecimal = BigDecimal.ZERO,
        maximumQuantity: BigDecimal? = null
    ): Stock {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("Produto não encontrado") }

        stockRepository.findByProductId(productId)?.let {
            throw IllegalStateException("Estoque já existe para este produto")
        }

        val stock = Stock(
            product = product,
            company = company,
            currentQuantity = initialQuantity,
            minimumQuantity = minimumQuantity,
            maximumQuantity = maximumQuantity,
            reservedQuantity = BigDecimal.ZERO
        )

        val savedStock = stockRepository.save(stock)

        registerMovement(
            product = product,
            company = company,
            type = MovementType.ADJUSTMENT_ADD,
            quantity = initialQuantity,
            previousQuantity = BigDecimal.ZERO,
            newQuantity = initialQuantity,
            reason = "Estoque inicial"
        )

        return savedStock
    }

    @Transactional
    fun addPurchase(
        productId: UUID,
        quantity: BigDecimal,
        company: Company?,
        user: AppUser?,
        costPerUnit: BigDecimal? = null,
        batchCode: String? = null,
        expiryDate: LocalDate? = null,
        reason: String? = null
    ): Stock {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("Produto não encontrado") }

        val stock = stockRepository.findByProductId(productId) ?: run {
            val newStock = Stock(
                product = product,
                company = company,
                currentQuantity = BigDecimal.ZERO,
                minimumQuantity = BigDecimal.ZERO,
                maximumQuantity = null,
                reservedQuantity = BigDecimal.ZERO
            )
            val savedStock = stockRepository.save(newStock)

            println("✅ Estoque criado automaticamente para o produto: ${product.name}")
            savedStock
        }

        val previousQuantity = stock.currentQuantity
        stock.currentQuantity = stock.currentQuantity.add(quantity)
        stock.totalPurchasedLifetime = stock.totalPurchasedLifetime.add(quantity)
        stock.lastMovementDate = LocalDateTime.now()
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        val movement = registerMovement(
            product = product,
            company = company,
            type = MovementType.PURCHASE,
            quantity = quantity,
            previousQuantity = previousQuantity,
            newQuantity = stock.currentQuantity,
            user = user,
            reason = reason ?: "Compra de estoque",
            costPerUnit = costPerUnit,
            expiryDate = expiryDate
        )

        if (expiryDate != null) {
            val batch = ProductExpiryBatch(
                product = product,
                company = company,
                batchCode = batchCode,
                expiryDate = expiryDate,
                quantity = quantity,
                initialQuantity = quantity,
                costPerUnit = costPerUnit
            )
            productExpiryBatchRepository.save(batch)
            movement.eventId = batch.id
            stockMovementRepository.save(movement)
        }

        return updatedStock
    }

    @Transactional
    fun reserveStock(productId: UUID, quantity: BigDecimal, saleId: UUID): Stock {
        val stock = getStockByProductId(productId)

        if (stock.availableQuantity < quantity) {
            throw InsufficientStockException(
                "Estoque insuficiente. Disponível: ${stock.availableQuantity}, Solicitado: $quantity"
            )
        }

        stock.reservedQuantity = stock.reservedQuantity.add(quantity)
        stock.lastMovementDate = LocalDateTime.now()
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        registerMovement(
            product = stock.product,
            company = stock.company,
            type = MovementType.RESERVATION,
            quantity = quantity,
            saleId = saleId,
            reason = "Reserva para venda"
        )

        return updatedStock
    }

    @Transactional
    fun confirmSale(productId: UUID, quantity: BigDecimal, saleId: UUID, user: AppUser?): Stock {
        val stock = getStockByProductId(productId)
        val previousQuantity = stock.currentQuantity

        stock.currentQuantity = stock.currentQuantity.subtract(quantity)
        stock.reservedQuantity = stock.reservedQuantity.subtract(quantity)
        stock.totalConsumedLifetime = stock.totalConsumedLifetime.add(quantity)
        stock.lastMovementDate = LocalDateTime.now()
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        registerMovement(
            product = stock.product,
            company = stock.company,
            type = MovementType.SALE,
            quantity = quantity,
            previousQuantity = previousQuantity,
            newQuantity = stock.currentQuantity,
            user = user,
            saleId = saleId,
            reason = "Venda confirmada"
        )

        updateExpiryBatchesOnSale(productId, quantity)

        return updatedStock
    }

    @Transactional
    fun registerLoss(
        productId: UUID,
        quantity: BigDecimal,
        company: Company?,
        user: AppUser?,
        reason: String,
        expiryDate: LocalDate? = null,
        batchId: UUID? = null
    ): Stock {
        // 1. Buscar o produto
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("Produto não encontrado") }

        // 2. VERIFICAR SE JÁ EXISTE ESTOQUE
        var stock = stockRepository.findByProductId(productId)

        if (stock == null) {
            // 🚨 Se não tem estoque, não pode dar saída!
            throw InsufficientStockException(
                "Não é possível registrar perda/saída. Estoque não existe para este produto. Registre uma entrada primeiro."
            )
        }

        // 3. Validar se tem quantidade suficiente
        if (stock.currentQuantity < quantity) {
            throw InsufficientStockException(
                "Estoque insuficiente. Disponível: ${stock.currentQuantity}, Solicitado: $quantity"
            )
        }

        // 4. Registrar a perda
        val previousQuantity = stock.currentQuantity
        stock.currentQuantity = stock.currentQuantity.subtract(quantity)
        stock.totalWastedLifetime = stock.totalWastedLifetime.add(quantity)
        stock.lastMovementDate = LocalDateTime.now()
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        registerMovement(
            product = product,
            company = company,
            type = MovementType.LOSS,
            quantity = quantity,
            previousQuantity = previousQuantity,
            newQuantity = stock.currentQuantity,
            user = user,
            reason = reason,
            expiryDate = expiryDate
        )

        // 5. Atualizar lote se informado
        if (batchId != null) {
            val batch = productExpiryBatchRepository.findById(batchId).orElse(null)
            batch?.let {
                it.quantity = it.quantity.subtract(quantity)
                if (it.quantity <= BigDecimal.ZERO) {
                    it.isActive = false
                }
                productExpiryBatchRepository.save(it)
            }
        }

        return updatedStock
    }

    // ============= OPERATION POINTS =============

    @Transactional
    fun registerOperation(
        company: Company?,
        request: RegisterOperationRequest
    ): OperationPoint {
        val operation = OperationPoint(
            company = company,
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate,
            expectedAudience = request.expectedAudience,
            numberOfCheckouts = request.numberOfCheckouts,
            operationType = request.operationType,
            isActive = true
        )

        val savedOperation = operationPointRepository.save(operation)

        var totalPurchasedCost = BigDecimal.ZERO
        var totalWastedCost = BigDecimal.ZERO

        request.consumptions.forEach { input ->
            val product = productRepository.findById(input.productId)
                .orElseThrow { ProductNotFoundException("Produto não encontrado: ${input.productId}") }

            val wasted = input.totalQuantityPurchased.subtract(input.totalQuantitySold)
            val wastePercentage = if (input.totalQuantityPurchased > BigDecimal.ZERO) {
                wasted.divide(input.totalQuantityPurchased, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .toDouble()
            } else 0.0

            val salesPerCheckout = if (request.numberOfCheckouts > 0) {
                input.totalQuantitySold.divide(
                    BigDecimal(request.numberOfCheckouts),
                    2,
                    RoundingMode.HALF_UP
                )
            } else BigDecimal.ZERO

            val purchaseCost = input.totalQuantityPurchased.multiply(product.price)
            val wastedCost = wasted.multiply(product.price)

            totalPurchasedCost = totalPurchasedCost.add(purchaseCost)
            totalWastedCost = totalWastedCost.add(wastedCost)

            val insight = generateOperationInsight(
                product.name,
                wastePercentage,
                wasted,
                request.operationType
            )

            val consumptionRecord = ConsumptionRecord(
                operationPoint = savedOperation,
                product = product,
                totalQuantitySold = input.totalQuantitySold,
                totalQuantityPurchased = input.totalQuantityPurchased,
                totalWasted = wasted,
                wastePercentage = wastePercentage,
                salesPerCheckout = salesPerCheckout,
                insight = insight,
                suggestionForNext = generateSuggestionForNext(
                    product.name,
                    wastePercentage,
                    input.totalQuantityPurchased,
                    input.totalQuantitySold
                )
            )

            consumptionRecordRepository.save(consumptionRecord)
        }

        return savedOperation
    }

    fun getAllOperations(companyId: UUID?): List<OperationResponse> {
        val operations = operationPointRepository.findByCompanyIdOrderByStartDateDesc(companyId)

        return operations.map { op ->
            val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(op.id)
            val totalSpent = records.sumOf { it.totalQuantityPurchased.multiply(it.product.price) }
            val totalWasted = records.sumOf { it.totalWasted.multiply(it.product.price) }
            val wastePercentage = if (totalSpent > BigDecimal.ZERO) {
                (totalWasted.divide(totalSpent, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .toDouble())
            } else 0.0

            OperationResponse(
                operationId = op.id,
                name = op.name,
                startDate = op.startDate,
                endDate = op.endDate,
                operationType = op.operationType,
                expectedAudience = op.expectedAudience,
                numberOfCheckouts = op.numberOfCheckouts,
                totalSpent = totalSpent,
                totalWasted = totalWasted,
                wastePercentage = wastePercentage,
                isActive = op.isActive
            )
        }
    }

    fun getOperationReport(operationId: UUID): OperationDetailResponse {
        val operation = operationPointRepository.findById(operationId)
            .orElseThrow { IllegalArgumentException("Operação não encontrada") }

        val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(operationId)

        val items = records.map { record ->
            val wasteCost = record.totalWasted.multiply(record.product.price)

            // ✅ CORREÇÃO: Calcular salesPerCheckout por produto corretamente
            val salesPerCheckout = if (operation.numberOfCheckouts > 0) {
                (record.totalQuantitySold ?: BigDecimal.ZERO).divide(
                    BigDecimal(operation.numberOfCheckouts),
                    2,
                    RoundingMode.HALF_UP
                )
            } else BigDecimal.ZERO

            OperationItemReport(
                productId = record.product.id,
                productName = record.product.name,
                purchasedQuantity = record.totalQuantityPurchased,
                soldQuantity = record.totalQuantitySold ?: BigDecimal.ZERO,
                wastedQuantity = record.totalWasted,
                wastePercentage = record.wastePercentage,
                // ✅ CORREÇÃO: Usar o valor calculado, não o do banco
                salesPerCheckout = salesPerCheckout,
                insight = record.insight,
                suggestionForNext = record.suggestionForNext,
                wasteCost = wasteCost
            )
        }

        val totalSpent = items.sumOf { it.purchasedQuantity.multiply(recordPrice(it.productId)) }
        val totalWasted = items.sumOf { it.wasteCost }

        val wastePercentage = if (totalSpent > BigDecimal.ZERO) {
            (totalWasted.divide(totalSpent, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP)
                .toDouble())
        } else 0.0

        // ✅ CORREÇÃO: Calcular totalSold corretamente
        val totalSold = items.sumOf { it.soldQuantity }

        // ✅ CORREÇÃO: Calcular salesPerCheckout geral
        val salesPerCheckout = if (operation.numberOfCheckouts > 0 && totalSold > BigDecimal.ZERO) {
            totalSold.divide(
                BigDecimal(operation.numberOfCheckouts),
                2,
                RoundingMode.HALF_UP
            )
        } else BigDecimal.ZERO

        val insights = generateOperationInsights(operation, items)

        return OperationDetailResponse(
            operationId = operation.id,
            name = operation.name,
            startDate = operation.startDate,
            endDate = operation.endDate,
            operationType = operation.operationType,
            expectedAudience = operation.expectedAudience,
            numberOfCheckouts = operation.numberOfCheckouts,
            totalSpent = totalSpent,
            totalWasted = totalWasted,
            wastePercentage = wastePercentage,
            items = items,
            insights = insights,
            salesPerCheckout = salesPerCheckout
        )
    }

    fun getOperationAnalysis(companyId: UUID, monthsBack: Int = 12): OperationAnalysisSummary {
        val operations = operationPointRepository.findByCompanyIdOrderByStartDateDesc(companyId)
            .filter { it.startDate!! >= LocalDateTime.now().minusMonths(monthsBack.toLong()) }

        if (operations.isEmpty()) {
            return OperationAnalysisSummary(
                companyId = companyId,
                companyName = "",
                totalOperationsAnalyzed = 0,
                averageAudience = 0,
                averageWastePercentage = 0.0,
                totalWasteCost = BigDecimal.ZERO,
                topWastedProducts = emptyList(),
                recommendations = listOf(
                    "Cadastre seus pontos de venda (estádios, shows, festivais) para receber análises personalizadas",
                    "Quanto mais operações você registrar, mais precisas serão as sugestões"
                )
            )
        }

        val totalOperations = operations.size
        val avgAudience = operations.mapNotNull { it.expectedAudience }.average().toInt()

        val wasteData = operations.mapNotNull { op ->
            val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(op.id)
            val totalSpent = records.sumOf { it.totalQuantityPurchased.multiply(it.product.price) }
            val totalWasted = records.sumOf { it.totalWasted.multiply(it.product.price) }
            if (totalSpent > BigDecimal.ZERO) {
                (totalWasted.divide(totalSpent, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .toDouble()) to totalWasted
            } else null
        }

        val avgWaste = wasteData.map { it.first }.average()
        val totalWasteCost = wasteData.map { it.second }.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }

        val topWasted = consumptionRecordRepository.findTopWastedProducts(companyId)
            .take(5)
            .map { result ->
                val productId = result[0] as UUID
                val totalWasted = result[1] as BigDecimal
                val wasteCost = result[2] as BigDecimal
                val product = productRepository.findById(productId).orElse(null)

                OperationProductWasteSummary(
                    productId = productId,
                    productName = product?.name ?: "Produto",
                    totalPurchased = BigDecimal.ZERO,
                    totalSold = BigDecimal.ZERO,
                    totalWasted = totalWasted,
                    wastePercentage = 0.0,
                    wasteCost = wasteCost,
                    recommendation = generateProductRecommendation(product?.name ?: "", 30.0)
                )
            }

        val recommendations = generateOperationRecommendations(operations, avgWaste)

        val companyName = operations.firstOrNull()?.company?.name ?: ""

        return OperationAnalysisSummary(
            companyId = companyId,
            companyName = companyName,
            totalOperationsAnalyzed = totalOperations,
            averageAudience = avgAudience,
            averageWastePercentage = avgWaste,
            totalWasteCost = totalWasteCost,
            topWastedProducts = topWasted,
            recommendations = recommendations
        )
    }

    fun getOperationPurchaseSuggestions(
        companyId: UUID,
        operationType: OperationType? = null
    ): OperationPurchaseSuggestion {
        val operations = if (operationType != null) {
            operationPointRepository.findByCompanyIdAndOperationType(companyId, operationType)
        } else {
            operationPointRepository.findByCompanyIdOrderByStartDateDesc(companyId)
        }

        if (operations.isEmpty()) {
            return OperationPurchaseSuggestion(
                companyId = companyId,
                generatedAt = LocalDateTime.now(),
                basedOnOperations = 0,
                suggestedPurchases = emptyList(),
                avoidPurchases = emptyList(),
                summary = "Cadastre seus pontos de venda para receber sugestões de compra personalizadas."
            )
        }

        val suggestions = mutableListOf<OperationPurchaseSuggestionItem>()
        val avoidPurchases = mutableListOf<OperationProductToAvoid>()

        val productConsumption = mutableMapOf<UUID, MutableList<BigDecimal>>()
        val productWaste = mutableMapOf<UUID, MutableList<Double>>()

        operations.forEach { op ->
            val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(op.id)
            records.forEach { record ->
                val perCheckout = if (op.numberOfCheckouts > 0) {
                    record.totalQuantitySold.divide(
                        BigDecimal(op.numberOfCheckouts),
                        2,
                        RoundingMode.HALF_UP
                    )
                } else BigDecimal.ZERO

                productConsumption.getOrPut(record.product.id as UUID) { mutableListOf() }
                    .add(perCheckout)

                productWaste.getOrPut(record.product.id) { mutableListOf() }
                    .add(record.wastePercentage)
            }
        }

        // Gerar sugestões de compra
        productConsumption.forEach { (productId, consumptions) ->
            if (consumptions.isNotEmpty()) {
                val avgConsumption = consumptions
                    .reduce { acc, value -> acc.add(value) }
                    .divide(BigDecimal(consumptions.size), 2, RoundingMode.HALF_UP)

                val avgCheckouts = operations.map { it.numberOfCheckouts }.average().toInt()

                if (avgCheckouts > 0) {
                    val suggestedTotal = avgConsumption.multiply(BigDecimal(avgCheckouts))
                        .multiply(BigDecimal("1.1"))
                        .setScale(0, RoundingMode.UP)

                    val product = productRepository.findById(productId).orElse(null)

                    if (product != null) {
                        val stock = stockRepository.findByProductId(productId)
                        val currentStock = stock?.currentQuantity ?: BigDecimal.ZERO

                        if (currentStock < suggestedTotal) {
                            val needed = suggestedTotal.subtract(currentStock).max(BigDecimal.ZERO)

                            if (needed > BigDecimal.ZERO) {
                                suggestions.add(
                                    OperationPurchaseSuggestionItem(
                                        productId = productId,
                                        productName = product.name,
                                        suggestedQuantity = needed,
                                        estimatedCost = needed.multiply(product.price),
                                        reason = "Baseado em ${consumptions.size} operações. Média por caixa: ${avgConsumption} unidades",
                                        confidence = 80 + (consumptions.size * 2).coerceAtMost(20)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Produtos para evitar
        productWaste.forEach { (productId, wastes) ->
            val avgWaste = wastes.average()
            if (avgWaste > 25.0) {
                productRepository.findById(productId).ifPresent { product ->
                    avoidPurchases.add(
                        OperationProductToAvoid(
                            productId = productId,
                            productName = product.name,
                            reason = "Desperdício médio de ${"%.1f".format(avgWaste)}% em operações anteriores",
                            historicalWaste = avgWaste
                        )
                    )
                }
            }
        }

        val summary = buildString {
            append("Com base em ${operations.size} operações, ")
            if (suggestions.isNotEmpty()) {
                append("recomendamos comprar ${suggestions.size} produtos. ")
            }
            if (avoidPurchases.isNotEmpty()) {
                append("Evite comprar ${avoidPurchases.size} produtos com alto histórico de desperdício.")
            }
        }

        return OperationPurchaseSuggestion(
            companyId = companyId,
            generatedAt = LocalDateTime.now(),
            basedOnOperations = operations.size,
            suggestedPurchases = suggestions.sortedByDescending { it.confidence }.take(5),
            avoidPurchases = avoidPurchases.distinctBy { it.productId }.take(5),
            summary = summary
        )
    }

    // ============= MÉTODOS AUXILIARES =============

    fun getStockByProductId(productId: UUID): Stock {
        return stockRepository.findByProductId(productId)
            ?: throw StockNotFoundException("Estoque não encontrado para o produto $productId")
    }

    fun getAllStocksByCompany(companyId: UUID?): List<Stock> {
        return stockRepository.findByCompanyId(companyId)
    }

    fun getLowStockItems(companyId: UUID?): List<LowStockResponse> {
        return stockRepository.findLowStockByCompanyId(companyId)
            .map { stock ->
                val deficit = stock.minimumQuantity.subtract(stock.currentQuantity)

                val safetyMargin = deficit.multiply(BigDecimal("0.20"))

                val recommendedPurchase = deficit.add(safetyMargin)

                LowStockResponse(
                    productId = stock.product.id,
                    productName = stock.product.name,
                    currentQuantity = stock.currentQuantity,
                    minimumQuantity = stock.minimumQuantity,
                    deficit = deficit,
                    recommendedPurchase = recommendedPurchase
                )
            }
    }

    fun getProductMovements(productId: UUID, days: Int): List<StockMovement> {
        val date = LocalDateTime.now().minusDays(days.toLong())
        return stockMovementRepository.findByProductIdAndMovementDateAfterOrderByMovementDateDesc(productId, date)
    }

    fun adjustMinimumStock(productId: UUID, minimumQuantity: BigDecimal): Stock {
        val stock = getStockByProductId(productId)
        stock.minimumQuantity = minimumQuantity
        stock.updatedAt = LocalDateTime.now()
        return stockRepository.save(stock)
    }

    fun getExpiringBatches(companyId: UUID): List<ProductExpiryBatch> {
        val expiringDate = LocalDate.now().plusDays(30)
        return productExpiryBatchRepository.findExpiringBatches(companyId, expiringDate)
    }

    fun getProductBatches(productId: UUID): List<ProductExpiryBatch> {
        return productExpiryBatchRepository.findByProductIdAndIsActiveTrue(productId)
    }

    fun getDashboardSummary(companyId: UUID): DashboardSummaryResponse {
        val totalProducts = getTotalProductsWithStock(companyId)
        val lowStockCount = getLowStockItems(companyId).size
        val expiringCount = getExpiringProductsCount(companyId)
        val totalWasteCost = getTotalWasteCost(companyId)
        val upcomingOperations = getUpcomingOperations(companyId)

        return DashboardSummaryResponse(
            totalProductsWithStock = totalProducts,
            lowStockAlerts = lowStockCount,
            expiringProductsAlerts = expiringCount,
            totalWasteCostLastYear = totalWasteCost,
            upcomingOperations = upcomingOperations.map { op ->
                UpcomingOperationResponse(
                    operationId = op.id,
                    name = op.name,
                    startDate = op.startDate ?: LocalDateTime.now(), // ✅ Seguro com ?:
                    operationType = op.operationType,
                    expectedAudience = op.expectedAudience ?: 0      // ✅ Seguro com ?:
                )
            }
        )
    }

    // ============= MÉTODOS PRIVADOS =============

    private fun registerMovement(
        product: Product,
        company: Company?,
        type: MovementType,
        quantity: BigDecimal,
        previousQuantity: BigDecimal? = null,
        newQuantity: BigDecimal? = null,
        user: AppUser? = null,
        saleId: UUID? = null,
        eventId: UUID? = null,
        reason: String? = null,
        observation: String? = null,
        expiryDate: LocalDate? = null,
        costPerUnit: BigDecimal? = null
    ): StockMovement {
        val movement = StockMovement(
            product = product,
            company = company,
            user = user,
            type = type,
            quantity = quantity,
            previousQuantity = previousQuantity,
            newQuantity = newQuantity,
            saleId = saleId,
            eventId = eventId,
            reason = reason,
            observation = observation,
            expiryDate = expiryDate,
            costPerUnit = costPerUnit
        )
        return stockMovementRepository.save(movement)
    }

    private fun updateExpiryBatchesOnSale(productId: UUID, quantitySold: BigDecimal) {
        val batches = productExpiryBatchRepository
            .findByProductIdAndIsActiveTrue(productId)
            .sortedBy { it.expiryDate }

        var remainingQuantity = quantitySold

        for (batch in batches) {
            if (remainingQuantity <= BigDecimal.ZERO) break

            val quantityToRemove = if (batch.quantity >= remainingQuantity) {
                remainingQuantity
            } else {
                batch.quantity
            }

            batch.quantity = batch.quantity.subtract(quantityToRemove)
            remainingQuantity = remainingQuantity.subtract(quantityToRemove)

            if (batch.quantity <= BigDecimal.ZERO) {
                batch.isActive = false
            }

            productExpiryBatchRepository.save(batch)
        }
    }

    private fun generateOperationInsight(
        productName: String,
        wastePercentage: Double,
        wastedQuantity: BigDecimal,
        operationType: OperationType
    ): String {
        return when {
            wastePercentage >= 30.0 -> {
                "⚠️ ALTO DESPERDÍCIO! ${wastedQuantity} unidades de $productName não foram vendidas. Reduza o estoque em 30% para ${
                    getOperationTypeName(
                        operationType
                    )
                }."
            }

            wastePercentage in 15.0..29.9 -> {
                "📉 Desperdício de ${"%.1f".format(wastePercentage)}% em $productName. Reduza um pouco a compra."
            }

            wastePercentage in 1.0..14.9 -> {
                "👏 Quase perfeito! Apenas ${"%.1f".format(wastePercentage)}% de desperdício."
            }

            wastePercentage == 0.0 -> {
                "🎯 PERFEITO! Vendeu tudo! Essa é a quantidade ideal."
            }

            else -> "✅ Consumo adequado."
        }
    }

    private fun generateSuggestionForNext(
        productName: String,
        wastePercentage: Double,
        purchased: BigDecimal,
        sold: BigDecimal
    ): String? {
        return when {
            wastePercentage >= 30.0 -> {
                "Para a próxima operação, compre ${
                    (purchased * BigDecimal("0.7")).setScale(
                        0,
                        RoundingMode.UP
                    )
                } unidades (-30%)"
            }

            wastePercentage in 15.0..29.9 -> {
                "Para a próxima operação, compre ${
                    (purchased * BigDecimal("0.85")).setScale(
                        0,
                        RoundingMode.UP
                    )
                } unidades (-15%)"
            }

            wastePercentage == 0.0 -> {
                "Mantenha a compra de ${purchased} unidades"
            }

            else -> null
        }
    }

    private fun generateOperationInsights(
        operation: OperationPoint?,
        items: List<OperationItemReport>
    ): List<String> {
        val insights = mutableListOf<String>()

        val highWasteItems = items.filter { it.wastePercentage > 20.0 }
        if (highWasteItems.isNotEmpty()) {
            insights.add("🎯 Produtos com maior desperdício: ${highWasteItems.take(2).joinToString { it.productName }}")
        }

        val lowWasteItems = items.filter { it.wastePercentage < 5.0 && it.soldQuantity > BigDecimal.ZERO }
        if (lowWasteItems.isNotEmpty()) {
            insights.add("✅ Acertou na quantidade de: ${lowWasteItems.take(2).joinToString { it.productName }}")
        }

        if (operation?.expectedAudience != null && operation.expectedAudience!! > 0) {
            val salesPerPerson = items.sumOf { it.soldQuantity.toDouble() } / operation.expectedAudience!!
            insights.add("👥 Média de vendas por pessoa: ${"%.2f".format(salesPerPerson)} unidades")
        }

        return insights
    }

    private fun generateOperationRecommendations(
        operations: List<OperationPoint>,
        avgWaste: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()

        recommendations.add("📊 Seu desperdício médio é de ${"%.1f".format(avgWaste)}%")

        if (avgWaste > 20.0) {
            recommendations.add("⚠️ Você está acima da média de desperdício. Reveja o planejamento de estoque.")
        } else if (avgWaste < 10.0) {
            recommendations.add("🏆 Excelente! Controle de desperdício muito bom!")
        }

        val operationTypes = operations.groupBy { it.operationType }
        if (operationTypes.size > 1) {
            val mostWasteful = operationTypes.maxByOrNull {
                val ops = it.value
                val wastes = ops.mapNotNull { op ->
                    val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(op.id)
                    val totalSpent = records.sumOf { r -> r.totalQuantityPurchased.multiply(r.product.price) }
                    val totalWasted = records.sumOf { r -> r.totalWasted.multiply(r.product.price) }
                    if (totalSpent > BigDecimal.ZERO) {
                        (totalWasted.divide(totalSpent, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal(100))
                            .toDouble())
                    } else null
                }
                wastes.average()
            }
            mostWasteful?.let {
                recommendations.add("📌 Seu maior desperdício é em ${getOperationTypeName(it.key)} - planeje melhor o estoque para este tipo")
            }
        }

        return recommendations
    }

    private fun getOperationTypeName(type: OperationType): String {
        return when (type) {
            OperationType.STADIUM -> "Estádios"
            OperationType.ARENA -> "Arenas"
            OperationType.SOCCER_MATCH -> "Jogos de Futebol"
            OperationType.BASKETBALL_GAME -> "Jogos de Basquete"
            OperationType.FIGHT_EVENT -> "Eventos de Luta"
            OperationType.CONCERT -> "Shows"
            OperationType.FESTIVAL -> "Festivais"
            OperationType.NIGHT_CLUB -> "Casas Noturnas"
            OperationType.FAIR -> "Feiras"
            OperationType.CIRCUS -> "Circos"
            OperationType.CARNIVAL_STREET -> "Carnaval"
            OperationType.JUNE_FESTIVAL -> "Festas Juninas"
            OperationType.OKTOBERFEST -> "Oktoberfest"
            OperationType.CHRISTMAS_MARKET -> "Feiras de Natal"
            OperationType.FOOD_TRUCK -> "Food Trucks"
            OperationType.TEMPORARY_BAR -> "Bares Temporários"
            OperationType.KIOSK -> "Quiosques"
            OperationType.BEACH -> "Praias"
            OperationType.PARK -> "Parques"
            OperationType.TOURIST_SPOT -> "Pontos Turísticos"
            OperationType.SUBWAY_STATION -> "Estações de Metrô"
            OperationType.BUS_STATION -> "Rodoviárias"
            OperationType.AIRPORT -> "Aeroportos"
            OperationType.CONVENTION_CENTER -> "Centros de Convenções"
            else -> "Outros"
        }
    }

    private fun recordPrice(productId: UUID?): BigDecimal {
        val product = productRepository.findById(productId).orElse(null)
        return product?.price ?: BigDecimal.ZERO
    }

    fun getTotalProductsWithStock(companyId: UUID): Long {
        return stockRepository.countByCompanyId(companyId)
    }

    fun getExpiringProductsCount(companyId: UUID): Int {
        val expiringDate = LocalDate.now().plusDays(7)
        return productExpiryBatchRepository.findExpiringBatches(companyId, expiringDate).size
    }

    fun getTotalWasteCost(companyId: UUID): BigDecimal {
        val sixMonthsAgo = LocalDateTime.now().minusMonths(12)
        val losses = stockMovementRepository.findLossesByCompanyAndDateAfter(companyId, sixMonthsAgo)
        return losses.map { it.quantity.multiply(it.product.price) }
            .fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
    }

    fun getUpcomingOperations(companyId: UUID): List<OperationPoint> {
        return operationPointRepository.findUpcomingOperations(companyId)
    }

    private fun generateProductRecommendation(productName: String, wastePercentage: Double): String {
        return when {
            wastePercentage >= 30.0 -> "Compre 30% MENOS deste produto"
            wastePercentage >= 15.0 -> "Compre 15% MENOS"
            wastePercentage >= 5.0 -> "Mantenha a quantidade, mas fique atento"
            else -> "Continue comprando esta quantidade"
        }
    }

    // ============= ALERTAS DE DESPERDÍCIO =============

    fun checkWasteAlerts(companyId: UUID): List<WasteAlert> {
        val alerts = mutableListOf<WasteAlert>()

        // 1. Alertas de vencimento (já existente)
        val expiringBatches = productExpiryBatchRepository.findExpiringBatches(
            companyId,
            LocalDate.now().plusDays(7)
        )

        expiringBatches.forEach { batch ->
            val daysLeft = batch.daysUntilExpiry.toInt()
            val product = batch.product
            val estimatedLoss = batch.quantity.multiply(product.price)

            when {
                daysLeft <= 0 -> {
                    alerts.add(
                        WasteAlert(
                            severity = AlertSeverity.CRITICAL,
                            title = "🚨 PRODUTO VENCIDO!",
                            message = "${product.name} - ${batch.quantity} unidades VENCIDAS",
                            suggestedAction = "Registrar como perda e remover do estoque",
                            estimatedLoss = estimatedLoss,
                            productId = product.id,
                            productName = product.name,
                            expiryDate = batch.expiryDate,
                            daysLeft = daysLeft
                        )
                    )
                }
                daysLeft <= 3 -> {
                    alerts.add(
                        WasteAlert(
                            severity = AlertSeverity.CRITICAL,
                            title = "🔥 VENCE EM $daysLeft DIAS!",
                            message = "${product.name}: ${batch.quantity} unidades vencem em $daysLeft dias",
                            suggestedAction = "Criar promoção relâmpago ou incluir em combo",
                            estimatedLoss = estimatedLoss,
                            productId = product.id,
                            productName = product.name,
                            expiryDate = batch.expiryDate,
                            daysLeft = daysLeft
                        )
                    )
                }
                daysLeft <= 7 -> {
                    alerts.add(
                        WasteAlert(
                            severity = AlertSeverity.HIGH,
                            title = "⚠️ Vence em $daysLeft dias",
                            message = "${product.name}: ${batch.quantity} unidades",
                            suggestedAction = "Priorizar venda e evitar novas compras",
                            estimatedLoss = estimatedLoss,
                            productId = product.id,
                            productName = product.name,
                            expiryDate = batch.expiryDate,
                            daysLeft = daysLeft
                        )
                    )
                }
            }
        }

        // 2. Alertas de operações com alto desperdício
        val operations = operationPointRepository.findByCompanyIdOrderByStartDateDesc(companyId)
            .filter { it.startDate?.isAfter(LocalDateTime.now().minusMonths(6)) ?: false }
            .take(5)

        operations.forEach { op ->
            val records = consumptionRecordRepository.findByOperationPointIdOrderByRecordDateDesc(op.id)
            val totalSpent = records.sumOf { it.totalQuantityPurchased.multiply(it.product.price) }
            val totalWasted = records.sumOf { it.totalWasted.multiply(it.product.price) }

            if (totalSpent > BigDecimal.ZERO) {
                val wastePercentage = totalWasted.divide(totalSpent, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .toDouble()

                if (wastePercentage > 20.0) {
                    alerts.add(
                        WasteAlert(
                            severity = AlertSeverity.MEDIUM,
                            title = "📊 Desperdício de ${"%.1f".format(wastePercentage)}%",
                            message = "Operação '${op.name}' teve R$ ${"%.2f".format(totalWasted)} em produtos não vendidos",
                            suggestedAction = "Analisar relatório da operação para ajustar compras futuras",
                            estimatedLoss = totalWasted
                        )
                    )
                }
            }
        }

        // 3. Alertas de produtos com alto desperdício histórico
        val topWasted = consumptionRecordRepository.findTopWastedProducts(companyId)
            .take(3)
            .mapNotNull { result ->
                val productId = result[0] as UUID
                val totalWasted = result[1] as BigDecimal
                val wasteCost = result[2] as BigDecimal
                val product = productRepository.findById(productId).orElse(null)

                if (product != null && totalWasted > BigDecimal.ZERO) {
                    WasteAlert(
                        severity = AlertSeverity.MEDIUM,
                        title = "⚠️ Produto com alto desperdício",
                        message = "${product.name}: ${totalWasted} unidades desperdiçadas",
                        suggestedAction = "Reduzir compra e revisar estoque",
                        estimatedLoss = wasteCost,
                        productId = productId,
                        productName = product.name
                    )
                } else null
            }

        alerts.addAll(topWasted)

        // 4. Alertas de estoque parado (já existente)
        val stagnantStock = checkStagnantStock(companyId)
        alerts.addAll(stagnantStock)

        return alerts.sortedByDescending { it.severity.ordinal }
    }

    private fun checkStagnantStock(companyId: UUID): List<WasteAlert> {
        val alerts = mutableListOf<WasteAlert>()
        val stocks = stockRepository.findByCompanyId(companyId)

        val sixMonthsAgo = LocalDateTime.now().minusMonths(6)

        stocks.forEach { stock ->
            if (stock.currentQuantity > BigDecimal.ZERO) {
                val lastMovement = stockMovementRepository
                    .findByProductIdOrderByMovementDateDesc(stock.product.id)
                    .firstOrNull()

                if (lastMovement == null || lastMovement.movementDate.isBefore(sixMonthsAgo)) {
                    alerts.add(
                        WasteAlert(
                            severity = AlertSeverity.MEDIUM,
                            title = "📦 Estoque parado",
                            message = "${stock.product.name}: ${stock.currentQuantity} unidades sem movimentação há 6+ meses",
                            suggestedAction = "Verificar validade ou criar promoção",
                            estimatedLoss = stock.currentQuantity.multiply(stock.product.price),
                            productId = stock.product.id,
                            productName = stock.product.name
                        )
                    )
                }
            }
        }

        return alerts
    }
}