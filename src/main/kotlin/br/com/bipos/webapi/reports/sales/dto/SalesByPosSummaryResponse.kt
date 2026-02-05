package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal

data class SalesByPosSummaryResponse(
    val posSerial: String,
    val totalReceived: BigDecimal,
    val salesCount: Long
)