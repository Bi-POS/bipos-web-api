package br.com.bipos.webapi.user

import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import br.com.bipos.webapi.init.SpacesProperties
import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserResponseDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import br.com.bipos.webapi.user.mapper.toDTO
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val companyRepository: CompanyRepository,
    private val passwordEncoder: PasswordEncoder,
    private val spacesProperties: SpacesProperties,
    private val s3Client: S3Client
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
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Senha é obrigatória"
            )
        }

        if (appUserRepository.existsByEmail(dto.email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "E-mail já cadastrado"
            )
        }

        val company = companyRepository.findById(companyId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa não encontrada"
                )
            }

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
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado"
            )

        if (user.role == UserRole.MANAGER || user.role == UserRole.OPERATOR) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Sem permissão para editar dados do usuário"
            )
        }

        user.name = dto.name.trim()
        user.email = dto.email.lowercase().trim()
        user.role = dto.role
        user.active = dto.active

        return appUserRepository.save(user).toDTO()
    }

    @Transactional
    fun updatePhoto(userId: UUID, file: MultipartFile) {

        if (file.isEmpty || !file.contentType.orEmpty().startsWith("image")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Arquivo inválido"
            )
        }

        val extension = file.originalFilename
            ?.substringAfterLast(".", "png")

        val key = "photos/users/user-$userId.$extension"

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(spacesProperties.bucket)
                .key(key)
                .contentType(file.contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build(),
            RequestBody.fromInputStream(file.inputStream, file.size)
        )

        val url = "${spacesProperties.cdn}/$key"

        val user = appUserRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado"
                )
            }

        user.photoUrl = url
        appUserRepository.save(user)
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
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado"
            )

        if (user.role != UserRole.OWNER) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Apenas o OWNER pode remover usuários"
            )
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
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado"
            )
}
