package br.com.bipos.webapi.stock.dto

import java.util.*

data class ProductToAvoid(
    val productId: UUID,
    val productName: String,
    val reason: String,
    val historicalWaste: Double
)