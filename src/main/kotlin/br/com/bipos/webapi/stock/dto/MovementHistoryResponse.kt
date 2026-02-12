package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class MovementHistoryResponse(
    val id: UUID?,
    val type: String,
    val typeDescription: String,
    val quantity: BigDecimal,
    val previousQuantity: BigDecimal?,
    val newQuantity: BigDecimal?,
    val reason: String?,
    val movementDate: LocalDateTime,
    val user: String?
)
