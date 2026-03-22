package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.util.UUID

data class LowStockResponse(
    val productId: UUID?,
    val productName: String,
    val currentQuantity: BigDecimal,
    val minimumQuantity: BigDecimal,
    val deficit: BigDecimal,
    val recommendedPurchase: BigDecimal
)