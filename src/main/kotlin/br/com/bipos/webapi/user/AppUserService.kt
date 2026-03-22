package br.com.bipos.webapi.user

import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import br.com.bipos.webapi.exception.BusinessException
import br.com.bipos.webapi.exception.ConflictException
import br.com.bipos.webapi.exception.ForbiddenOperationException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.storage.SpacesStorageService
import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import br.com.bipos.webapi.user.mapper.toDTO
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val companyRepository: CompanyRepository,
    private val passwordEncoder: PasswordEncoder,
    private val storageService: SpacesStorageService
) {
    /* =========================
       CREATE
       ========================= */

    @Transactional
    fun create(
        dto: UserCreateDTO,
        companyId: UUID
    ): UserResponseDTO {

        if (dto.password.isBlank()) {
            throw BusinessException("Senha é obrigatória")
        }

        if (appUserRepository.existsByEmail(dto.email)) {
            throw ConflictException("E-mail já cadastrado")
        }

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        val user = AppUser(
            name = dto.name.trim(),
            email = dto.email.lowercase().trim(),
            passwordHash = passwordEncoder.encode(dto.password),
            role = dto.role,
            company = company,
            active = true
        )

        return appUserRepository.save(user).toDTO()
    }


    /* =========================
       UPDATE
       ========================= */

    @Transactional
    fun update(
        userId: UUID,
        dto: UserUpdateDTO,
        companyId: UUID
    ): UserResponseDTO {

        val user = appUserRepository.findByIdAndCompanyId(userId, companyId)
            ?: throw ResourceNotFoundException("Usuário não encontrado")

        if (user.role == UserRole.MANAGER || user.role == UserRole.OPERATOR) {
            throw ForbiddenOperationException("Sem permissão para editar dados do usuário")
        }

        user.name = dto.name.trim()
        user.email = dto.email.lowercase().trim()
        user.role = dto.role
        user.active = dto.active

        return appUserRepository.save(user).toDTO()
    }

    @Transactional
    fun updatePhoto(userId: UUID, companyId: UUID, file: MultipartFile): UserResponseDTO {

        if (file.isEmpty || !file.contentType.orEmpty().startsWith("image")) {
            throw BusinessException("Arquivo inválido")
        }

        val extension = file.originalFilename
            ?.substringAfterLast(".", "png")

        val key = "photos/users/user-$userId.$extension"
        val url = storageService.uploadPublicFile(key, file)

        val user = appUserRepository.findByIdAndCompanyId(userId, companyId)
            ?: throw ResourceNotFoundException("Usuário não encontrado")

        user.photoUrl = url
        user.updatePhotoAt = Instant.now()

        return appUserRepository.save(user).toDTO()
    }


    /* =========================
       DELETE
       ========================= */

    @Transactional
    fun delete(
        userId: UUID,
        companyId: UUID
    ) {
        val user = appUserRepository.findByIdAndCompanyId(userId, companyId)
            ?: throw ResourceNotFoundException("Usuário não encontrado")

        if (user.role != UserRole.OWNER) {
            throw ForbiddenOperationException("Apenas o OWNER pode remover usuários")
        }

        appUserRepository.delete(user)
    }

    /* =========================
       LIST
       ========================= */

    fun list(companyId: UUID): List<UserResponseDTO> =
        appUserRepository
            .findAllByCompanyIdAndRoleNot(companyId, UserRole.OWNER)
            .map { it.toDTO() }

    fun getById(
        userId: UUID,
        companyId: UUID
    ): UserResponseDTO =
        appUserRepository.findByIdAndCompanyId(userId, companyId)
            ?.toDTO()
            ?: throw ResourceNotFoundException("Usuário não encontrado")
}
