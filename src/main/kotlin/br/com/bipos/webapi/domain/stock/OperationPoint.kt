package br.com.bipos.webapi.domain.stock

import br.com.bipos.webapi.domain.company.Company
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "operation_points")
class OperationPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company?,

    @Column(nullable = false)
    var name: String,                    // Ex: "Estádio do Maracanã", "Rock in Rio 2026"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var operationType: OperationType,    // Tipo de operação

    @Column(name = "start_date")
    var startDate: LocalDateTime?,      // Início da operação

    @Column(name = "end_date")
    var endDate: LocalDateTime?,        // Fim da operação (se temporário)

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "expected_audience")
    var expectedAudience: Int? = null,  // Público esperado

    @Column(name = "number_of_checkouts")
    var numberOfCheckouts: Int = 1,     // Quantos caixas/máquinas

    @OneToMany(mappedBy = "operationPoint", cascade = [CascadeType.ALL])
    val consumptionRecords: MutableList<ConsumptionRecord> = mutableListOf(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

