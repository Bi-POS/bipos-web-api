package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal

data class PosByUserResponse(
    val posSerial: String,
    val operatorName: String,
    val totalReceived: BigDecimal,
    val paymentsCount: Long
)
