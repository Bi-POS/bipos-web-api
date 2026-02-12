package br.com.bipos.webapi.stock.dto

import br.com.bipos.webapi.domain.stock.OperationType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// ============= REQUESTS =============

data class RegisterOperationRequest(
    val name: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime? = null,
    val expectedAudience: Int,
    val numberOfCheckouts: Int,
    val operationType: OperationType,
    val consumptions: List<OperationConsumptionInput>
)

data class OperationConsumptionInput(
    val productId: UUID,
    val totalQuantityPurchased: BigDecimal,
    val totalQuantitySold: BigDecimal
)

// ============= RESPONSES =============

data class OperationResponse(
    val operationId: UUID?,
    val name: String,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val operationType: OperationType,
    val expectedAudience: Int?,
    val numberOfCheckouts: Int,
    val totalSpent: BigDecimal,
    val totalWasted: BigDecimal,
    val wastePercentage: Double,
    val isActive: Boolean
)

data class OperationDetailResponse(
    val operationId: UUID?,
    val name: String,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val operationType: OperationType,
    val expectedAudience: Int?,
    val numberOfCheckouts: Int,
    val totalSpent: BigDecimal,
    val totalWasted: BigDecimal,
    val wastePercentage: Double,
    val items: List<OperationItemReport>,
    val insights: List<String>,
    val salesPerCheckout: BigDecimal
)

data class OperationItemReport(
    val productId: UUID?,
    val productName: String,
    val purchasedQuantity: BigDecimal,
    val soldQuantity: BigDecimal,
    val wastedQuantity: BigDecimal,
    val wastePercentage: Double,
    val salesPerCheckout: BigDecimal?,
    val insight: String?,
    val suggestionForNext: String?,
    val wasteCost: BigDecimal
)

// ============= DASHBOARD =============

data class DashboardSummaryResponse(
    val totalProductsWithStock: Long,
    val lowStockAlerts: Int,
    val expiringProductsAlerts: Int,
    val totalWasteCostLastYear: BigDecimal,
    val upcomingOperations: List<UpcomingOperationResponse>
)

// ✅ NOVO DTO - Próximas operações
data class UpcomingOperationResponse(
    val operationId: UUID?,
    val name: String,
    val startDate: LocalDateTime,
    val operationType: OperationType,
    val expectedAudience: Int
)

// ============= ANÁLISE =============

data class OperationAnalysisSummary(
    val companyId: UUID,
    val companyName: String,
    val totalOperationsAnalyzed: Int,
    val averageAudience: Int,
    val averageWastePercentage: Double,
    val totalWasteCost: BigDecimal,
    val topWastedProducts: List<OperationProductWasteSummary>,
    val recommendations: List<String>
)

data class OperationProductWasteSummary(
    val productId: UUID,
    val productName: String,
    val totalPurchased: BigDecimal,
    val totalSold: BigDecimal,
    val totalWasted: BigDecimal,
    val wastePercentage: Double,
    val wasteCost: BigDecimal,
    val recommendation: String
)

// ============= SUGESTÕES =============

data class OperationPurchaseSuggestion(
    val companyId: UUID,
    val generatedAt: LocalDateTime,
    val basedOnOperations: Int,
    val suggestedPurchases: List<OperationPurchaseSuggestionItem>,
    val avoidPurchases: List<OperationProductToAvoid>,
    val summary: String
)

data class OperationPurchaseSuggestionItem(
    val productId: UUID,
    val productName: String,
    val suggestedQuantity: BigDecimal,
    val estimatedCost: BigDecimal,
    val reason: String,
    val confidence: Int
)

data class OperationProductToAvoid(
    val productId: UUID,
    val productName: String,
    val reason: String,
    val historicalWaste: Double
)