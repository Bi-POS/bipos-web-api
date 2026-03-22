package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.stock.OperationType
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.security.requireCompanyRef
import br.com.bipos.webapi.stock.dto.*
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/stock")
class StockController(
    private val stockService: StockService
) {

    // ==========================
    // HELPERS (ajuste conforme seu AppUserDetails)
    // ==========================
    private fun companyRef(user: AppUserDetails): Company =
        user.requireCompanyRef()

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
    ): ResponseEntity<StockDetailResponse> {
        val stock = stockService.initializeStock(
            productId = productId,
            company = companyRef(user),
            initialQuantity = initialQuantity,
            minimumQuantity = minimumQuantity,
            maximumQuantity = maximumQuantity
        )
        return ResponseEntity.ok(stock.toStockDetailResponse())
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
    ): ResponseEntity<StockDetailResponse> {
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
        return ResponseEntity.ok(stock.toStockDetailResponse())
    }

    @PostMapping("/products/{productId}/reserve")
    fun reserveStock(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam saleId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<StockDetailResponse> {
        val stock = stockService.reserveStock(productId, user.requireCompanyId(), quantity, saleId)
        return ResponseEntity.ok(stock.toStockDetailResponse())
    }

    @PostMapping("/products/{productId}/confirm-sale")
    fun confirmSale(
        @PathVariable productId: UUID,
        @RequestParam quantity: BigDecimal,
        @RequestParam saleId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<StockDetailResponse> {
        val stock = stockService.confirmSale(
            productId = productId,
            companyId = user.requireCompanyId(),
            quantity = quantity,
            saleId = saleId,
            user = user.user
        )
        return ResponseEntity.ok(stock.toStockDetailResponse())
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
    ): ResponseEntity<StockDetailResponse> {
        val stock = stockService.registerLoss(
            productId = productId,
            quantity = quantity,
            company = companyRef(user),
            user = user.user,
            reason = reason,
            expiryDate = expiryDate,
            batchId = batchId
        )
        return ResponseEntity.ok(stock.toStockDetailResponse())
    }

    @GetMapping("/products/{productId}")
    fun getStockByProduct(
        @PathVariable productId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<StockDetailResponse> {
        return ResponseEntity.ok(
            stockService.getStockByProductId(productId, user.requireCompanyId()).toStockDetailResponse()
        )
    }

    @GetMapping("/company")
    fun getAllStocks(@CurrentUser user: AppUserDetails): ResponseEntity<List<StockResponse>> {
        val stocks = stockService.getAllStocksByCompany(user.requireCompanyId())

        val response = stocks.map { stock ->
            stock.toStockResponse(
                message = if (stock.isLowStock) "Estoque abaixo do minimo" else null
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/low-stock")
    fun getLowStock(@CurrentUser user: AppUserDetails): ResponseEntity<List<LowStockResponse>> {
        return ResponseEntity.ok(stockService.getLowStockItems(user.requireCompanyId()))
    }

    @GetMapping("/products/{productId}/movements")
    fun getMovements(
        @PathVariable productId: UUID,
        @RequestParam(defaultValue = "30") days: Int,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<StockMovementResponse>> {

        val movements = stockService.getProductMovements(productId, user.requireCompanyId(), days)

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
        val updatedStock = stockService.adjustMinimumStock(
            productId = productId,
            companyId = user.requireCompanyId(),
            minimumQuantity = minimumQuantity
        )

        return ResponseEntity.ok(
            updatedStock.toStockResponse(
                message = "Quantidade minima atualizada para $minimumQuantity unidades"
            )
        )
    }

    // ==========================
    // LOTES / VENCIMENTO
    // ==========================
    @GetMapping("/batches/expiring")
    fun getExpiringBatches(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<ExpiryBatchResponse>> {
        return ResponseEntity.ok(
            stockService.getExpiringBatches(user.requireCompanyId()).map { it.toExpiryBatchResponse() }
        )
    }

    @GetMapping("/products/{productId}/batches")
    fun getProductBatches(
        @PathVariable productId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<ExpiryBatchResponse>> {
        return ResponseEntity.ok(
            stockService.getProductBatches(productId, user.requireCompanyId()).map { it.toExpiryBatchResponse() }
        )
    }

    // ==========================
    // DASHBOARD / ALERTAS
    // ==========================
    @GetMapping("/dashboard/summary")
    fun dashboardSummary(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<DashboardSummaryResponse> {
        return ResponseEntity.ok(stockService.getDashboardSummary(user.requireCompanyId()))
    }

    @GetMapping("/alerts/waste")
    fun wasteAlerts(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<WasteAlert>> {
        return ResponseEntity.ok(stockService.checkWasteAlerts(user.requireCompanyId()))
    }

    // ==========================
    // OPERAÇÕES (OperationPoint)
    // ==========================
    @PostMapping("/operations")
    fun registerOperation(
        @RequestBody request: RegisterOperationRequest,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationPointResponse> {
        val op = stockService.registerOperation(companyRef(user), request)
        return ResponseEntity.ok(op.toOperationPointResponse())
    }

    @GetMapping("/operations")
    fun getOperations(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<OperationResponse>> {
        return ResponseEntity.ok(stockService.getAllOperations(user.requireCompanyId()))
    }

    @GetMapping("/operations/{operationId}")
    fun getOperationReport(
        @PathVariable operationId: UUID,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationDetailResponse> {
        return ResponseEntity.ok(stockService.getOperationReport(operationId, user.requireCompanyId()))
    }

    @GetMapping("/operations/analysis")
    fun getOperationAnalysis(
        @RequestParam(defaultValue = "12") monthsBack: Int,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationAnalysisSummary> {
        return ResponseEntity.ok(stockService.getOperationAnalysis(user.requireCompanyId(), monthsBack))
    }

    @GetMapping("/operations/purchase-suggestions")
    fun getOperationPurchaseSuggestions(
        @RequestParam(required = false) operationType: OperationType?,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<OperationPurchaseSuggestion> {
        return ResponseEntity.ok(
            stockService.getOperationPurchaseSuggestions(
                companyId = user.requireCompanyId(),
                operationType = operationType
            )
        )
    }
}
