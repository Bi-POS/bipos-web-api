package br.com.bipos.webapi.sale.product.dto

import br.com.bipos.webapi.domain.catalog.UnitType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SaleProductCreateDTO(

    @field:NotBlank
    val name: String,

    @field:NotNull
    val price: BigDecimal,

    @field:NotNull
    val unitType: UnitType,

    val imageUrl: String? = null
)
