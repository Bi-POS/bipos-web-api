package br.com.bipos.webapi.domain.stock

import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.domain.company.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "stocks")
class Stock(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company?,

    @Column(nullable = false)
    var currentQuantity: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var minimumQuantity: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var maximumQuantity: BigDecimal? = null,

    @Column(nullable = false)
    var reservedQuantity: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_purchased_lifetime")
    var totalPurchasedLifetime: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_consumed_lifetime")
    var totalConsumedLifetime: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_wasted_lifetime")
    var totalWastedLifetime: BigDecimal = BigDecimal.ZERO,

    @Column(name = "last_movement_date")
    var lastMovementDate: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val availableQuantity: BigDecimal
        get() = currentQuantity.subtract(reservedQuantity)

    val isLowStock: Boolean
        get() = currentQuantity <= minimumQuantity

    val totalSpentLifetime: BigDecimal
        get() = totalPurchasedLifetime.multiply(product.price)
}