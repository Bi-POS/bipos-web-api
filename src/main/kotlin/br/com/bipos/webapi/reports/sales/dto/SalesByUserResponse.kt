package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal
import java.util.UUID

data class SalesByUserResponse(
    val userId: UUID,
    val operatorName: String,
    val totalReceived: BigDecimal,
    val salesCount: Long
)