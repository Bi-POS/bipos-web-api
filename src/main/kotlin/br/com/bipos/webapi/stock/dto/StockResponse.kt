package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.util.*

data class StockResponse(
    val message: String,
    val productId: UUID,
    val productName: String,
    val currentQuantity: BigDecimal,
    val minimumQuantity: BigDecimal,
    val availableQuantity: BigDecimal,
    val isLowStock: Boolean
)