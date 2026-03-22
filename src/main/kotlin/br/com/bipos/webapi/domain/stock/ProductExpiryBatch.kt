package br.com.bipos.webapi.domain.stock

import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.domain.company.Company
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "product_expiry_batches")
class ProductExpiryBatch(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @Column(name = "batch_code")
    var batchCode: String? = null,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDate,

    @Column(nullable = false, precision = 10, scale = 3)
    var quantity: BigDecimal,

    @Column(name = "initial_quantity", nullable = false, precision = 10, scale = 3)
    val initialQuantity: BigDecimal,

    @Column(name = "purchase_date")
    val purchaseDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    var costPerUnit: BigDecimal? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val daysUntilExpiry: Long
        get() {
            val today = LocalDate.now()
            return if (expiryDate.isAfter(today)) {
                today.until(expiryDate, ChronoUnit.DAYS)
            } else {
                -today.until(expiryDate, ChronoUnit.DAYS)
            }
        }

    val isExpiringSoon: Boolean
        get() = daysUntilExpiry <= 7 && daysUntilExpiry > 0

    val isExpired: Boolean
        get() = daysUntilExpiry <= 0

    val totalValue: BigDecimal
        get() = quantity.multiply(costPerUnit ?: BigDecimal.ZERO)
}
