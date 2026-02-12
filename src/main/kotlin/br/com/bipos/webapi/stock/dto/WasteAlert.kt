package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class WasteAlert(
    val alertId: UUID = UUID.randomUUID(),
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val suggestedAction: String,
    val estimatedLoss: BigDecimal? = null,
    val productId: UUID? = null,
    val productName: String? = null,
    val expiryDate: LocalDate? = null,
    val daysLeft: Int? = null
)
