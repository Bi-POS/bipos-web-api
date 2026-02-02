package br.com.bipos.webapi.user.mapper

import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.domain.user.AppUser

fun AppUser.toDTO() = UserResponseDTO(
    id = this.id,
    name = this.name,
    email = this.email,
    role = this.role,
    active = this.active,
    photoUrl = null,
    createdAt = this.createdAt
)
