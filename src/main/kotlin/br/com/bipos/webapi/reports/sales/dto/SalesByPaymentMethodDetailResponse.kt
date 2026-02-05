package br.com.bipos.webapi.reports.sales.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class SalesByPaymentMethodDetailResponse(
    val saleId: UUID,
    val date: LocalDate,
    val receivedAmount: BigDecimal,
    val itemsCount: Int,
    val products: List<String>
)