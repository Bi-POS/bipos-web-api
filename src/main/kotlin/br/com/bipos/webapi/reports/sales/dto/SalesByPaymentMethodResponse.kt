package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal

data class SalesByPaymentMethodResponse(
    val method: String,
    val total: BigDecimal,
    val count: Long
)