package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class SalesByPosDetailResponse(
    val saleId: UUID,
    val date: LocalDate,
    val userId: UUID,
    val userName: String,
    val method: String,
    val totalReceived: BigDecimal,
    val products: List<String>
)
