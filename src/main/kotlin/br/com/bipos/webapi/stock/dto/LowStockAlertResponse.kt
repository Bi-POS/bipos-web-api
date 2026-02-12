package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.util.*

data class LowStockAlertResponse(
    val productId: UUID?,
    val productName: String,
    val currentQuantity: BigDecimal,
    val minimumQuantity: BigDecimal,
    val deficit: BigDecimal,
    val recommendedPurchase: BigDecimal
)