package br.com.bipos.webapi.graphic.dto

import java.math.BigDecimal
import java.util.*

data class SalesByOperatorResponse(
    val userId: UUID?,
    val operatorName: String,
    val total: BigDecimal
)
