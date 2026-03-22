package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.stock.MovementType
import br.com.bipos.webapi.domain.stock.OperationPoint
import br.com.bipos.webapi.domain.stock.OperationType
import br.com.bipos.webapi.domain.stock.ProductExpiryBatch
import br.com.bipos.webapi.domain.stock.Stock
import br.com.bipos.webapi.domain.stock.StockMovement
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.exception.ConflictException
import br.com.bipos.webapi.exception.InsufficientStockException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.stock.dto.DashboardSummaryResponse
import br.com.bipos.webapi.stock.dto.LowStockResponse
import br.com.bipos.webapi.stock.dto.OperationAnalysisSummary
import br.com.bipos.webapi.stock.dto.OperationDetailResponse
import br.com.bipos.webapi.stock.dto.OperationPurchaseSuggestion
import br.com.bipos.webapi.stock.dto.OperationResponse
import br.com.bipos.webapi.stock.dto.RegisterOperationRequest
import br.com.bipos.webapi.stock.dto.WasteAlert
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class StockService(
    private val stockRepository: StockRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val productExpiryBatchRepository: ProductExpiryBatchRepository,
    private val stockLookupService: StockLookupService,
    private val stockQueryService: StockQueryService,
    private val stockOperationService: StockOperationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StockService::class.java)
    }

    @Transactional
    fun initializeStock(
        productId: UUID,
        company: Company,
        initialQuantity: BigDecimal,
        minimumQuantity: BigDecimal = BigDecimal.ZERO,
        maximumQuantity: BigDecimal? = null
    ): Stock {
        val companyId = stockLookupService.requireCompanyId(company)
        val product = stockLookupService.getProductForCompany(productId, companyId)

        stockRepository.findByProductIdAndCompanyId(productId, companyId)?.let {
            throw ConflictException("Estoque já existe para este produto")
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
        company: Company,
        user: AppUser?,
        costPerUnit: BigDecimal? = null,
        batchCode: String? = null,
        expiryDate: LocalDate? = null,
        reason: String? = null
    ): Stock {
        val companyId = stockLookupService.requireCompanyId(company)
        val product = stockLookupService.getProductForCompany(productId, companyId)

        val stock = stockRepository.findByProductIdAndCompanyId(productId, companyId) ?: run {
            val newStock = Stock(
                product = product,
                company = company,
                currentQuantity = BigDecimal.ZERO,
                minimumQuantity = BigDecimal.ZERO,
                maximumQuantity = null,
                reservedQuantity = BigDecimal.ZERO
            )
            val savedStock = stockRepository.save(newStock)

            logger.info("Stock created automatically for product {}", product.name)
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
    fun reserveStock(productId: UUID, companyId: UUID, quantity: BigDecimal, saleId: UUID): Stock {
        val stock = stockLookupService.getStockByProductId(productId, companyId)

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
    fun confirmSale(productId: UUID, companyId: UUID, quantity: BigDecimal, saleId: UUID, user: AppUser?): Stock {
        val stock = stockLookupService.getStockByProductId(productId, companyId)
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

        updateExpiryBatchesOnSale(productId, companyId, quantity)

        return updatedStock
    }

    @Transactional
    fun registerLoss(
        productId: UUID,
        quantity: BigDecimal,
        company: Company,
        user: AppUser?,
        reason: String,
        expiryDate: LocalDate? = null,
        batchId: UUID? = null
    ): Stock {
        val companyId = stockLookupService.requireCompanyId(company)
        val product = stockLookupService.getProductForCompany(productId, companyId)
        val stock = stockLookupService.getStockByProductId(productId, companyId)

        if (stock.currentQuantity < quantity) {
            throw InsufficientStockException(
                "Estoque insuficiente. Disponível: ${stock.currentQuantity}, Solicitado: $quantity"
            )
        }

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

        if (batchId != null) {
            val batch = productExpiryBatchRepository.findByIdAndCompanyId(batchId, companyId)
                ?: throw ResourceNotFoundException("Lote não encontrado")

            if (batch.product.id != productId) {
                throw ResourceNotFoundException("Lote não encontrado para o produto informado")
            }

            batch.quantity = batch.quantity.subtract(quantity)
            if (batch.quantity <= BigDecimal.ZERO) {
                batch.isActive = false
            }
            productExpiryBatchRepository.save(batch)
        }

        return updatedStock
    }

    fun registerOperation(company: Company, request: RegisterOperationRequest): OperationPoint =
        stockOperationService.registerOperation(company, request)

    fun getAllOperations(companyId: UUID): List<OperationResponse> =
        stockOperationService.getAllOperations(companyId)

    fun getOperationReport(operationId: UUID, companyId: UUID): OperationDetailResponse =
        stockOperationService.getOperationReport(operationId, companyId)

    fun getOperationAnalysis(companyId: UUID, monthsBack: Int = 12): OperationAnalysisSummary =
        stockOperationService.getOperationAnalysis(companyId, monthsBack)

    fun getOperationPurchaseSuggestions(
        companyId: UUID,
        operationType: OperationType? = null
    ): OperationPurchaseSuggestion =
        stockOperationService.getOperationPurchaseSuggestions(companyId, operationType)

    fun getStockByProductId(productId: UUID, companyId: UUID): Stock =
        stockQueryService.getStockByProductId(productId, companyId)

    fun getAllStocksByCompany(companyId: UUID): List<Stock> =
        stockQueryService.getAllStocksByCompany(companyId)

    fun getLowStockItems(companyId: UUID): List<LowStockResponse> =
        stockQueryService.getLowStockItems(companyId)

    fun getProductMovements(productId: UUID, companyId: UUID, days: Int): List<StockMovement> =
        stockQueryService.getProductMovements(productId, companyId, days)

    fun adjustMinimumStock(productId: UUID, companyId: UUID, minimumQuantity: BigDecimal): Stock {
        val stock = stockLookupService.getStockByProductId(productId, companyId)
        stock.minimumQuantity = minimumQuantity
        stock.updatedAt = LocalDateTime.now()
        return stockRepository.save(stock)
    }

    fun getExpiringBatches(companyId: UUID): List<ProductExpiryBatch> =
        stockQueryService.getExpiringBatches(companyId)

    fun getProductBatches(productId: UUID, companyId: UUID): List<ProductExpiryBatch> =
        stockQueryService.getProductBatches(productId, companyId)

    fun getDashboardSummary(companyId: UUID): DashboardSummaryResponse =
        stockQueryService.getDashboardSummary(companyId)

    fun checkWasteAlerts(companyId: UUID): List<WasteAlert> =
        stockQueryService.checkWasteAlerts(companyId)

    private fun registerMovement(
        product: Product,
        company: Company,
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

    private fun updateExpiryBatchesOnSale(productId: UUID, companyId: UUID, quantitySold: BigDecimal) {
        val batches = productExpiryBatchRepository
            .findByProductIdAndCompanyIdAndIsActiveTrue(productId, companyId)
            .sortedBy { it.expiryDate }

        var remainingQuantity = quantitySold

        for (batch in batches) {
            if (remainingQuantity <= BigDecimal.ZERO) {
                break
            }

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
}
