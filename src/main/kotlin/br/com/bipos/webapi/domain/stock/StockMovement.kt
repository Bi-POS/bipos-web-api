package br.com.bipos.webapi.domain.stock

import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.user.AppUser
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "stock_movements")
class StockMovement(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: AppUser? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MovementType,

    @Column(nullable = false, precision = 10, scale = 3)
    val quantity: BigDecimal,

    @Column(name = "previous_quantity", precision = 10, scale = 3)
    val previousQuantity: BigDecimal? = null,

    @Column(name = "new_quantity", precision = 10, scale = 3)
    val newQuantity: BigDecimal? = null,

    @Column(name = "sale_id")
    var saleId: UUID? = null,

    @Column(name = "event_id")
    var eventId: UUID? = null,

    @Column(length = 500)
    val reason: String? = null,

    @Column(length = 500)
    val observation: String? = null,

    @Column(name = "expiry_date")
    val expiryDate: LocalDate? = null,

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    var costPerUnit: BigDecimal? = null,

    @Column(name = "movement_date")
    val movementDate: LocalDateTime = LocalDateTime.now()
)

