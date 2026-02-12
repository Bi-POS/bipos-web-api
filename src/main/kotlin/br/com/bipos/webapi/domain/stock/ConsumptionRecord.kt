package br.com.bipos.webapi.domain.stock

import br.com.bipos.webapi.domain.catalog.Product
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "consumption_records")
class ConsumptionRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_point_id", nullable = false)
    val operationPoint: OperationPoint,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "total_quantity_sold", nullable = false)
    var totalQuantitySold: BigDecimal,      // Quantidade total vendida no período

    @Column(name = "total_quantity_purchased", nullable = false)
    var totalQuantityPurchased: BigDecimal, // Quantidade comprada para o evento

    @Column(name = "total_wasted", nullable = false)
    var totalWasted: BigDecimal,           // Quantidade desperdiçada/perdida

    @Column(name = "waste_percentage")
    var wastePercentage: Double,           // % de desperdício

    @Column(name = "sales_per_checkout")
    var salesPerCheckout: BigDecimal? = null, // Média de vendas por caixa

    @Column(name = "record_date")
    val recordDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "insight")
    var insight: String? = null,

    @Column(name = "suggestion_for_next")
    var suggestionForNext: String? = null
)