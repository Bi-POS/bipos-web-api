package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class SalesByPaymentMethodProductDayResponse(
    val date: LocalDate,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val totalReceived: BigDecimal
)