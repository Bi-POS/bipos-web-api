package br.com.bipos.webapi.sale.group.dto

import jakarta.validation.constraints.NotBlank

data class SaleGroupCreateDTO(

    @field:NotBlank
    val name: String,

    val imageUrl: String? = null
)
