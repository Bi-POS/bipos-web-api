package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class StockDetailResponse(
    val productId: UUID?,
    val productName: String,
    val currentQuantity: BigDecimal,
    val reservedQuantity: BigDecimal,
    val availableQuantity: BigDecimal,
    val minimumQuantity: BigDecimal,
    val maximumQuantity: BigDecimal?,
    val isLowStock: Boolean,
    val totalPurchasedLifetime: BigDecimal,
    val totalConsumedLifetime: BigDecimal,
    val totalWastedLifetime: BigDecimal,
    val lastMovementDate: LocalDateTime?
)