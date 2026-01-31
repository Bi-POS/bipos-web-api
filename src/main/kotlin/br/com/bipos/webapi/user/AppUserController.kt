package br.com.bipos.webapi.user

import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val appUserService: AppUserService
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','OPERATOR')")
    fun getById(
        @PathVariable id: UUID,
        @CurrentUser currentUser: AppUser
    ): ResponseEntity<UserResponseDTO> {

        val user = appUserService.getById(id, currentUser.company?.id!!)
        return ResponseEntity.ok(user)
    }

    /* =========================
       LISTAR USUÁRIOS
       ========================= */

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    fun list(
        @CurrentUser user: AppUser
    ): ResponseEntity<List<UserResponseDTO>> {

        val users = appUserService.list(user.company?.id!!)
        return ResponseEntity.ok(users)
    }

    /* =========================
       CRIAR USUÁRIO
       ========================= */

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun create(
        @Valid @RequestBody dto: UserCreateDTO,
        @CurrentUser user: AppUser
    ): ResponseEntity<UserResponseDTO> {

        val created = appUserService.create(dto, user.company?.id!!)
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
    ): ResponseEntity<Void> {

        appUserService.updatePhoto(id, file)
        return ResponseEntity.noContent().build()
    }

    /* =========================
       ATUALIZAR USUÁRIO
       ========================= */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: UserUpdateDTO,
        @CurrentUser user: AppUser
    ): ResponseEntity<UserResponseDTO> {

        val updated = appUserService.update(id, dto, user.company?.id!!)
        return ResponseEntity.ok(updated)
    }

    /* =========================
       DELETAR USUÁRIO
       ========================= */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    fun delete(
        @PathVariable id: UUID,
        @CurrentUser user: AppUser
    ): ResponseEntity<Void> {

        appUserService.delete(id, user.company?.id!!)
        return ResponseEntity.noContent().build()
    }
}
