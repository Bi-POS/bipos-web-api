package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class ExpiryBatchResponse(
    val batchId: UUID?,
    val productId: UUID?,
    val productName: String,
    val batchCode: String?,
    val expiryDate: LocalDate,
    val daysUntilExpiry: Long,
    val quantity: BigDecimal,
    val initialQuantity: BigDecimal,
    val costPerUnit: BigDecimal?,
    val totalValue: BigDecimal
)
