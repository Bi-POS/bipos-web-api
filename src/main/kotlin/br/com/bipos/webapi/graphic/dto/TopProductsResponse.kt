package br.com.bipos.webapi.graphic.dto

import java.math.BigDecimal
import java.util.*

data class TopProductsResponse(
    val productId: UUID,
    val productName: String,
    val total: BigDecimal
)
