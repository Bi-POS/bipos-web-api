package br.com.bipos.webapi.user

import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val appUserService: AppUserService
) {

    /* =========================
       BUSCAR USUÁRIO POR ID
       ========================= */

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','OPERATOR')")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal details: AppUserDetails?
    ): ResponseEntity<UserResponseDTO> {

        val currentUser = details?.user
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"
            )

        val companyId = currentUser.company?.id
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário sem empresa vinculada"
            )

        val user = appUserService.getById(id, companyId)
        return ResponseEntity.ok(user)
    }

    /* =========================
       LISTAR USUÁRIOS
       ========================= */

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    fun list(
        @AuthenticationPrincipal details: AppUserDetails?
    ): ResponseEntity<List<UserResponseDTO>> {

        val currentUser = details?.user
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"
            )

        val companyId = currentUser.company?.id
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário sem empresa vinculada"
            )

        val users = appUserService.list(companyId)
        return ResponseEntity.ok(users)
    }

    /* =========================
       CRIAR USUÁRIO
       ========================= */

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun create(
        @Valid @RequestBody dto: UserCreateDTO,
        @AuthenticationPrincipal details: AppUserDetails?
    ): ResponseEntity<UserResponseDTO> {

        val currentUser = details?.user
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"
            )

        val companyId = currentUser.company?.id
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário sem empresa vinculada"
            )

        val created = appUserService.create(dto, companyId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /* =========================
       ATUALIZAR FOTO
       ========================= */

    @PutMapping("/{id}/photo")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun updatePhoto(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UserResponseDTO> {

        val updatedUser = appUserService.updatePhoto(id, file)
        return ResponseEntity.ok(updatedUser)
    }

    /* =========================
       ATUALIZAR USUÁRIO
       ========================= */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: UserUpdateDTO,
        @AuthenticationPrincipal details: AppUserDetails?
    ): ResponseEntity<UserResponseDTO> {

        val currentUser = details?.user
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"
            )

        val companyId = currentUser.company?.id
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário sem empresa vinculada"
            )

        val updated = appUserService.update(id, dto, companyId)
        return ResponseEntity.ok(updated)
    }

    /* =========================
       DELETAR USUÁRIO
       ========================= */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal details: AppUserDetails?
    ): ResponseEntity<Void> {

        val currentUser = details?.user
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"
            )

        val companyId = currentUser.company?.id
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário sem empresa vinculada"
            )

        appUserService.delete(id, companyId)
        return ResponseEntity.noContent().build()
    }
}
