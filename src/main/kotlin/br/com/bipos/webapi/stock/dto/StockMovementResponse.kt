package br.com.bipos.webapi.stock.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class StockMovementResponse(
    val id: UUID?,

    val productId: UUID?,
    val productName: String,

    val movementType: String,     // enum name (SALE, PURCHASE...)
    val movementLabel: String,    // descrição amigável
    val signal: Int,              // +1 entrada | -1 saída

    val quantity: BigDecimal,
    val previousQuantity: BigDecimal?,
    val newQuantity: BigDecimal?,

    val costPerUnit: BigDecimal?,

    val reason: String?,
    val observation: String?,

    val saleId: UUID?,
    val eventId: UUID?,

    val expiryDate: LocalDate?,

    val userName: String?,

    val movementDate: LocalDateTime
)
