package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class StockSummaryResponse(
    val productId: UUID?,
    val productName: String,
    val currentQuantity: BigDecimal,
    val availableQuantity: BigDecimal,
    val isLowStock: Boolean,
    val lastMovementDate: LocalDateTime?
)