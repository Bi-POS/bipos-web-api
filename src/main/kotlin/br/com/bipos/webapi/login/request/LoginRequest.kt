package br.com.bipos.webapi.login.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "O useremail é obrigatório")
    val useremail: String,

    @field:NotBlank(message = "A senha é obrigatória")
    val password: String
)