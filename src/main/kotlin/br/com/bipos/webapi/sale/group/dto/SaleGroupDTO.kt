package br.com.bipos.webapi.sale.group.dto

import java.util.UUID

data class SaleGroupDTO(
    val id: UUID?,
    val name: String,
    val imageUrl: String?
)
