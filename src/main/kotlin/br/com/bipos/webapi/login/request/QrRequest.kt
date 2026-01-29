package br.com.bipos.webapi.login.request

import jakarta.validation.constraints.NotBlank

data class QrRequest(
    @field:NotBlank(message = "O email é obrigatório")
    val company: String
)
