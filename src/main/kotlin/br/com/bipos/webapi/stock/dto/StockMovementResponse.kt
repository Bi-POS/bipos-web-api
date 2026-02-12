package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class StockMovementResponse(
    val message: String,
    val productId: UUID,
    val productName: String,
    val quantity: BigDecimal,
    val newQuantity: BigDecimal,
    val movementType: String,
    val batchCode: String? = null,
    val expiryDate: LocalDate? = null,
    val reason: String? = null
)