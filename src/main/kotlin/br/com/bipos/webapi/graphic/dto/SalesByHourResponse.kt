package br.com.bipos.webapi.graphic.dto

import java.math.BigDecimal

data class SalesByHourResponse(
    val hour: Int,
    val total: BigDecimal
)
