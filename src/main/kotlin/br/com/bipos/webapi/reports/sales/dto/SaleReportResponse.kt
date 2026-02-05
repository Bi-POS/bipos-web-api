package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal

data class SalesReportResponse(
    val period: String,
    val total: BigDecimal,
    val count: Long
)