package br.com.bipos.webapi.user

import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/users", "/api/v1/users")
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
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<UserResponseDTO> =
        ResponseEntity.ok(appUserService.getById(id, details.requireCompanyId()))

    /* =========================
       LISTAR USUÁRIOS
       ========================= */

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    fun list(
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<List<UserResponseDTO>> =
        ResponseEntity.ok(appUserService.list(details.requireCompanyId()))

    /* =========================
       CRIAR USUÁRIO
       ========================= */

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun create(
        @Valid @RequestBody dto: UserCreateDTO,
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<UserResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(appUserService.create(dto, details.requireCompanyId()))

    /* =========================
       ATUALIZAR FOTO
       ========================= */

    @PutMapping("/{id}/photo")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    fun updatePhoto(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile,
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<UserResponseDTO> {

        val updatedUser = appUserService.updatePhoto(id, details.requireCompanyId(), file)
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
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<UserResponseDTO> =
        ResponseEntity.ok(appUserService.update(id, dto, details.requireCompanyId()))

    /* =========================
       DELETAR USUÁRIO
       ========================= */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    fun delete(
        @PathVariable id: UUID,
        @CurrentUser details: AppUserDetails
    ): ResponseEntity<Void> {
        appUserService.delete(id, details.requireCompanyId())
        return ResponseEntity.noContent().build()
    }
}
