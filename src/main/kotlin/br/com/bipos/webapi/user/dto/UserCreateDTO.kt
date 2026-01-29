package br.com.bipos.webapi.user.dto

import br.com.bipos.webapi.domain.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateDTO(
    @field:NotBlank val name: String,

    @field:Email val email: String,

    @field:Size(min = 6) val password: String,

    val role: UserRole,

    val photoUrl: String? = null,
)
