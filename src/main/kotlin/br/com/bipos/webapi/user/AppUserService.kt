package br.com.bipos.webapi.user

import br.com.bipos.webapi.user.dto.UserCreateDTO
import br.com.bipos.webapi.user.dto.UserUpdateDTO
import br.com.bipos.webapi.user.mapper.toDTO
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import br.com.bipos.webapi.company.CompanyRepository
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.collections.map

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val companyRepository: CompanyRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val userUploadBaseDir = Paths.get("/var/www/bipos/uploads/users")
    private val publicUserBasePath = "/uploads/users"

    /* =========================
       CREATE
       ========================= */

    @Transactional
    fun create(
        dto: UserCreateDTO,
        companyId: UUID
    ) = run {

        if (appUserRepository.existsByEmail(dto.email)) {
            throw IllegalArgumentException("E-mail já cadastrado")
        }

        val company = companyRepository.findById(companyId)
            .orElseThrow { IllegalArgumentException("Empresa não encontrada") }

        val user = AppUser(
            name = dto.name.trim(),
            email = dto.email.lowercase().trim(),
            passwordHash = passwordEncoder.encode(dto.password),
            role = dto.role,
            company = company,
            active = true
        )

        appUserRepository.save(user).toDTO()
    }

    /* =========================
       UPDATE
       ========================= */

    @Transactional
    fun update(
        userId: UUID,
        dto: UserUpdateDTO,
        companyId: UUID
    ) = run {

        val user = appUserRepository.findByIdAndCompanyId(userId, companyId)
            ?: throw IllegalArgumentException("Usuário não encontrado")

        if (user.role == UserRole.MANAGER || user.role == UserRole.OPERATOR) {
            throw AccessDeniedException("Sem permissão para editar dados do usuário")
        }
        user.name = dto.name.trim()
        user.email = dto.email.lowercase().trim()
        user.role = dto.role
        user.active = dto.active

        appUserRepository.save(user).toDTO()
    }

    @Transactional
    fun updatePhoto(userId: UUID, file: MultipartFile) {

        if (file.isEmpty) {
            throw IllegalArgumentException("Arquivo inválido")
        }

        if (!file.contentType.orEmpty().startsWith("image")) {
            throw IllegalArgumentException("Apenas imagens são permitidas")
        }

        val extension = file.originalFilename
            ?.substringAfterLast(".", "png")

        val fileName = "user-$userId.$extension"

        Files.createDirectories(userUploadBaseDir)
        val physicalPath = userUploadBaseDir.resolve(fileName)

        Files.copy(
            file.inputStream,
            physicalPath,
            StandardCopyOption.REPLACE_EXISTING
        )

        val user = appUserRepository.findById(userId)
            .orElseThrow { RuntimeException("Usuário não encontrado") }

        // 🌍 PATH PÚBLICO (vai pro banco)
        user.photoUrl = "$publicUserBasePath/$fileName"

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
            ?: throw IllegalArgumentException("Usuário não encontrado")

        if (user.role != UserRole.OWNER) {
            throw AccessDeniedException("Apenas o OWNER pode remover usuários")
        }
        appUserRepository.delete(user)
    }

    fun getById(
        userId: UUID,
        companyId: UUID
    ) = appUserRepository.findByIdAndCompanyId(userId, companyId)
        ?.toDTO()
        ?: throw IllegalArgumentException("Usuário não encontrado")

    /* =========================
       LIST
       ========================= */

    fun list(companyId: UUID) =
        appUserRepository
            .findAllByCompanyIdAndRoleNot(companyId, UserRole.OWNER)
            .map { it.toDTO() }
}
