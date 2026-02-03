package br.com.bipos.webapi.login.dto

import jakarta.validation.constraints.NotBlank

data class QrRequest(
    @field:NotBlank(message = "CompanyId é obrigatório")
    val companyId: String
)
