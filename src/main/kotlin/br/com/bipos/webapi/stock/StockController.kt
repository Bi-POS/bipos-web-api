package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.stock.*
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.SecurityUtils
import br.com.bipos.webapi.stock.dto.*
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/stock")
class StockController(
    private val stockService: StockService,
    private val stockRepository: StockRepository
) {

    // ==========================
    // HELPERS (ajuste conforme seu AppUserDetails)
    // ==========================
    private fun companyId(user: AppUserDetails): UUID =
        user.user.company?.id ?: throw IllegalStateException("Usuário sem companyId na sessão")

    private fun companyRef(user: AppUserDetails): Company =
        Company(id = companyId(user))

    // ==========================
    // ESTOQUE - OPERAÇÕES BÁSICAS
    // ==========================

    @PostMapping("/products/{productId}/initialize")
    fun initializeStock(
        @PathVariable productId: UUID,
        @RequestParam initialQuantity: BigDecimal,
        @RequestParam(required = false, defaultValue = "0") minimumQuantity: BigDecimal,
        @RequestParam(required = false) maximumQuantity: BigDecimal?,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<Stock> {
        val stock = stockService.initializeStock(
            productId = productId,
            company = companyRef(user),
            initialQuantity = initialQuantity,
            minimumQuantity = minimumQuantity,
            maximumQuantity = maximumQuantity
        )
        return ResponseEntity.ok(stock)
    }

    @PostMapping("/products/{productId}/purchase")
    fun addPurchase(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam(required = false) costPerUnit: BigDecimal?,
        @RequestParam(required = false) batchCode: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        expiryDate: LocalDate?,
        @RequestParam(required = false) reason: String?,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<Stock> {
        val stock = stockService.addPurchase(
            productId = productId,
            quantity = quantity,
            company = companyRef(user),
            user = user.user,
            costPerUnit = costPerUnit,
            batchCode = batchCode,
            expiryDate = expiryDate,
            reason = reason
        )
        return ResponseEntity.ok(stock)
    }

    @PostMapping("/products/{productId}/reserve")
    fun reserveStock(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam saleId: UUID
    ): ResponseEntity<Stock> {
        val stock = stockService.reserveStock(productId, quantity, saleId)
        return ResponseEntity.ok(stock)
    }

    @PostMapping("/products/{productId}/confirm-sale")
    fun confirmSale(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam saleId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<Stock> {
        val stock = stockService.confirmSale(
            productId = productId,
            quantity = quantity,
            saleId = saleId,
            user = user.user
        )
        return ResponseEntity.ok(stock)
    }

    @PostMapping("/products/{productId}/loss")
    fun registerLoss(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam reason: String,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        expiryDate: LocalDate?,
        @RequestParam(required = false) batchId: UUID?,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<Stock> {
        val stock = stockService.registerLoss(
            productId = productId,
            quantity = quantity,
            company = companyRef(user),
            user = user.user,
            reason = reason,
            expiryDate = expiryDate,
            batchId = batchId
        )
        return ResponseEntity.ok(stock)
    }

    @GetMapping("/products/{productId}")
    fun getStockByProduct(
        @PathVariable productId: UUID
    ): ResponseEntity<Stock> {
        return ResponseEntity.ok(stockService.getStockByProductId(productId))
    }

    @GetMapping("/company")
    fun getAllStocks(): ResponseEntity<List<StockResponse>> {

        val companyId = SecurityUtils.getCompanyId()

        val stocks = stockService.getAllStocksByCompany(companyId)

        val response = stocks.map { stock ->

            val minimum = stock.minimumQuantity ?: BigDecimal.ZERO
            val isLow = stock.currentQuantity <= minimum

            StockResponse(
                productId = stock.product.id,
                productName = stock.product.name,
                currentQuantity = stock.currentQuantity,
                availableQuantity = stock.availableQuantity ?: stock.currentQuantity,
                minimumQuantity = stock.minimumQuantity,
                isLowStock = isLow,
                message = if (isLow) {
                    "Estoque abaixo do mínimo"
                } else null
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/low-stock")
    fun getLowStock(@CurrentUser user: AppUserDetails): ResponseEntity<List<LowStockResponse>> {
        return ResponseEntity.ok(stockService.getLowStockItems(user.user.company?.id))
    }

    @GetMapping("/products/{productId}/movements")
    fun getMovements(
        @PathVariable productId: UUID,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<List<StockMovementResponse>> {

        val movements = stockService.getProductMovements(productId, days)

        val response = movements.map { movement ->
            StockMovementResponse(
                id = movement.id,

                productId = movement.product.id,
                productName = movement.product.name,

                movementType = movement.type.name,
                movementLabel = movement.type.description,
                signal = movement.type.signal,

                quantity = movement.quantity,
                previousQuantity = movement.previousQuantity,
                newQuantity = movement.newQuantity,

                costPerUnit = movement.costPerUnit,

                reason = movement.reason,
                observation = movement.observation,

                saleId = movement.saleId,
                eventId = movement.eventId,

                expiryDate = movement.expiryDate,

                userName = movement.user?.name,

                movementDate = movement.movementDate
            )
        }

        return ResponseEntity.ok(response)
    }


    // ==========================
    // AJUSTE DE MÍNIMO (SEU ENDPOINT)
    // ==========================
    @PostMapping("/products/{productId}/set-minimum")
    fun setMinimumStock(
        @PathVariable productId: UUID,
        @RequestParam minimumQuantity: BigDecimal,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<StockResponse> {
        val stock = stockService.getStockByProductId(productId)

        stock.minimumQuantity = minimumQuantity
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        return ResponseEntity.ok(
            StockResponse(
                message = "✅ Quantidade mínima atualizada para $minimumQuantity unidades",
                productId = productId,
                productName = updatedStock.product.name,
                currentQuantity = updatedStock.currentQuantity,
                minimumQuantity = updatedStock.minimumQuantity,
                availableQuantity = updatedStock.availableQuantity,
                isLowStock = updatedStock.isLowStock
            )
        )
    }

    // ==========================
    // LOTES / VENCIMENTO
    // ==========================
    @GetMapping("/batches/expiring")
    fun getExpiringBatches(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<ProductExpiryBatch>> {
        return ResponseEntity.ok(stockService.getExpiringBatches(companyId(user)))
    }

    @GetMapping("/products/{productId}/batches")
    fun getProductBatches(
        @PathVariable productId: UUID
    ): ResponseEntity<List<ProductExpiryBatch>> {
        return ResponseEntity.ok(stockService.getProductBatches(productId))
    }

    // ==========================
    // DASHBOARD / ALERTAS
    // ==========================
    @GetMapping("/dashboard/summary")
    fun dashboardSummary(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<DashboardSummaryResponse> {
        return ResponseEntity.ok(stockService.getDashboardSummary(companyId(user)))
    }

    @GetMapping("/alerts/waste")
    fun wasteAlerts(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<WasteAlert>> {
        return ResponseEntity.ok(stockService.checkWasteAlerts(companyId(user)))
    }

    // ==========================
    // OPERAÇÕES (OperationPoint)
    // ==========================
    @PostMapping("/operations")
    fun registerOperation(
        @RequestBody request: RegisterOperationRequest,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationPoint> {
        val op = stockService.registerOperation(companyRef(user), request)
        return ResponseEntity.ok(op)
    }

    @GetMapping("/operations")
    fun getOperations(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<OperationResponse>> {
        return ResponseEntity.ok(stockService.getAllOperations(companyId(user)))
    }

    @GetMapping("/operations/{operationId}")
    fun getOperationReport(
        @PathVariable operationId: UUID
    ): ResponseEntity<OperationDetailResponse> {
        return ResponseEntity.ok(stockService.getOperationReport(operationId))
    }

    @GetMapping("/operations/analysis")
    fun getOperationAnalysis(
        @RequestParam(defaultValue = "12") monthsBack: Int,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationAnalysisSummary> {
        return ResponseEntity.ok(stockService.getOperationAnalysis(companyId(user), monthsBack))
    }

    @GetMapping("/operations/purchase-suggestions")
    fun getOperationPurchaseSuggestions(
        @RequestParam(required = false) operationType: OperationType?,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationPurchaseSuggestion> {
        return ResponseEntity.ok(
            stockService.getOperationPurchaseSuggestions(
                companyId = companyId(user),
                operationType = operationType
            )
        )
    }
}