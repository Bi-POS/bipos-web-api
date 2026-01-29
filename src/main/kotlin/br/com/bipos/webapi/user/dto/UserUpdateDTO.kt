package br.com.bipos.webapi.user.dto

import br.com.bipos.webapi.domain.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserUpdateDTO(
    @field:NotBlank
    val name: String,

    @field:Email
    val email: String,

    val role: UserRole,

    val active: Boolean
)
