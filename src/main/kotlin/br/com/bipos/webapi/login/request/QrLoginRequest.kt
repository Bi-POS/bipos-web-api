package br.com.bipos.webapi.login.request

import jakarta.validation.constraints.NotBlank

data class QrLoginRequest(
    @field:NotBlank(message = "O token QR é obrigatório")
    val token: String
)
