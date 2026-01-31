package br.com.bipos.webapi.company

import br.com.bipos.webapi.company.dto.CompanyCreateDTO
import br.com.bipos.webapi.company.dto.CompanyDTO
import br.com.bipos.webapi.companymodule.CompanyModuleRepository
import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.company.CompanyStatus
import br.com.bipos.webapi.domain.companymodule.CompanyModule
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import br.com.bipos.webapi.domain.utils.DocumentType
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.user.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val moduleRepository: ModuleRepository,
    private val companyModuleRepository: CompanyModuleRepository,
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val uploadBaseDir = Paths.get("/var/www/bipos/uploads/logos")
    private val publicBasePath = "/uploads/logos"

    /* =========================
       CREATE
       ========================= */
    @Transactional
    fun create(dto: CompanyCreateDTO): CompanyDTO {

        require(!companyRepository.existsByEmail(dto.email)) {
            "Email já cadastrado"
        }

        require(!companyRepository.existsByDocument(dto.document)) {
            "Documento já cadastrado"
        }

        val company = companyRepository.save(
            Company(
                name = dto.name,
                email = dto.email,
                document = dto.document,
                documentType = DocumentType.CNPJ,
                phone = dto.phone,
                status = CompanyStatus.ACTIVE
            )
        )

        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: error("Módulo SALE não cadastrado")

        companyModuleRepository.save(
            CompanyModule(company = company, module = saleModule)
        )

        appUserRepository.save(
            AppUser(
                company = company,
                name = dto.ownerName,
                email = dto.ownerEmail,
                passwordHash = passwordEncoder.encode(dto.ownerPassword),
                role = UserRole.OWNER
            )
        )

        return toDTO(company)
    }

    /* =========================
       READ
       ========================= */
    fun list(): List<CompanyDTO> =
        companyRepository.findAll().map(::toDTO)

    fun getById(id: UUID?): CompanyDTO =
        companyRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Empresa não encontrada") }
            .let(::toDTO)

    /* =========================
       DELETE
       ========================= */
    @Transactional
    fun delete(id: UUID) {
        companyModuleRepository.deleteAllByCompanyId(id)
        companyRepository.deleteById(id)
    }

    /* =========================
       UPLOAD LOGO (CRÍTICO)
       ========================= */
    @Transactional
    fun updateLogo(companyId: UUID, file: MultipartFile) {

        require(!file.isEmpty) { "Arquivo inválido" }
        require(file.contentType?.startsWith("image") == true) {
            "Apenas imagens são permitidas"
        }

        val extension = when (file.contentType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Formato não suportado")
        }

        val fileName = "company-$companyId.$extension"

        Files.createDirectories(uploadBaseDir)
        val physicalPath = uploadBaseDir.resolve(fileName)

        Files.copy(
            file.inputStream,
            physicalPath,
            StandardCopyOption.REPLACE_EXISTING
        )

        // 🔐 PERMISSÕES (resolve o 500)
        Files.setPosixFilePermissions(
            physicalPath,
            setOf(
                java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                java.nio.file.attribute.PosixFilePermission.GROUP_READ,
                java.nio.file.attribute.PosixFilePermission.OTHERS_READ
            )
        )

        val company = companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Empresa não encontrada") }

        company.logoUrl = "$publicBasePath/$fileName"
        companyRepository.save(company)
    }

    /* =========================
       DTO
       ========================= */
    private fun toDTO(company: Company) = CompanyDTO(
        id = company.id!!,
        name = company.name,
        email = company.email,
        document = company.document,
        documentType = company.documentType,
        phone = company.phone,
        logoUrl = company.logoUrl,
        status = company.status.name
    )
}

