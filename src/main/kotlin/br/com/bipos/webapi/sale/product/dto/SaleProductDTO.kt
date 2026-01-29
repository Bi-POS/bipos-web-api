package br.com.bipos.webapi.sale.product.dto

import br.com.bipos.webapi.domain.catalog.UnitType
import java.math.BigDecimal
import java.util.*

data class SaleProductDTO(
    val id: UUID?,
    val name: String,
    val price: BigDecimal,
    val unitType: UnitType,
    val imageUrl: String?,
    val groupId: UUID?
)
