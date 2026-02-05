package br.com.bipos.webapi.user.mapper

import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.domain.user.AppUser
import java.time.ZoneOffset

fun AppUser.toDTO() = UserResponseDTO(
    id = this.id,
    name = this.name,
    email = this.email,
    role = this.role,
    active = this.active,
    photoUrl = photoUrl,
    updatePhotoAt = this.updatePhotoAt ?: this.createdAt.toInstant(ZoneOffset.UTC),
    createdAt = this.createdAt
)
