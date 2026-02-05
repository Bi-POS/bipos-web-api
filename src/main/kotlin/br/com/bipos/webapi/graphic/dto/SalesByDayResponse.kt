package br.com.bipos.webapi.graphic.dto

import java.math.BigDecimal
import java.time.LocalDate

data class SalesByDayResponse(
    val date: LocalDate,
    val total: BigDecimal
)
