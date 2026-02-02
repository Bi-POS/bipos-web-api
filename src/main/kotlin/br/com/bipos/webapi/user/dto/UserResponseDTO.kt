package br.com.bipos.webapi.user.dto

import br.com.bipos.webapi.domain.user.UserRole
import java.time.LocalDateTime
import java.util.*

data class UserResponseDTO(
    val id: UUID?,
    val name: String,
    val email: String,
    val role: UserRole,
    val active: Boolean,
    val photoUrl: String?,
    val createdAt: LocalDateTime
)
