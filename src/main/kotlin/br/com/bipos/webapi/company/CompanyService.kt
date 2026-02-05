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
import br.com.bipos.webapi.init.SpacesProperties
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.user.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Instant
import java.util.*

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val moduleRepository: ModuleRepository,
    private val companyModuleRepository: CompanyModuleRepository,
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val spacesProperties: SpacesProperties,
    private val s3Client: S3Client
) {

    @Transactional
    fun create(dto: CompanyCreateDTO): CompanyDTO {

        if (companyRepository.existsByEmail(dto.email)) {
            throw IllegalArgumentException("Email já cadastrado")
        }

        if (companyRepository.existsByDocument(dto.document)) {
            throw IllegalArgumentException("Documento já cadastrado")
        }

        val company = companyRepository.save(
            Company(
                name = dto.name,
                email = dto.email,
                document = dto.document,
                documentType = DocumentType.CNPJ,
                phone = dto.phone,
                status = CompanyStatus.ACTIVE,
            )
        )

        // 🔹 módulo SALE padrão
        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: throw IllegalStateException("Módulo SALE não cadastrado")

        companyModuleRepository.save(
            CompanyModule(
                company = company,
                module = saleModule
            )
        )

        // 🔥 cria USER OWNER automaticamente
        val owner = AppUser(
            company = company,
            name = dto.ownerName,
            email = dto.ownerEmail,
            passwordHash = passwordEncoder.encode(dto.ownerPassword),
            role = UserRole.OWNER
        )

        appUserRepository.save(owner)

        return toDTO(company)
    }

    fun list(): List<CompanyDTO> =
        companyRepository.findAll().map { toDTO(it) }

    fun getById(id: UUID?): CompanyDTO =
        companyRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Empresa não encontrada") }
            .let { toDTO(it) }

    @Transactional
    fun delete(id: UUID) {
        companyModuleRepository.deleteAllByCompanyId(id)
        companyRepository.deleteById(id)
    }

    private fun toDTO(company: Company) = CompanyDTO(
        id = company.id,
        name = company.name,
        email = company.email,
        document = company.document,
        documentType = company.documentType,
        phone = company.phone,
        logoUrl = company.logoUrl,
        updateLogoAt = company.updateLogoAt,
        status = company.status.name
    )
    
    fun updateLogo(companyId: UUID, file: MultipartFile) {

        if (file.isEmpty || !file.contentType.orEmpty().startsWith("image")) {
            throw IllegalArgumentException("Arquivo inválido")
        }

        val extension = file.originalFilename?.substringAfterLast(".", "png")
        val key = "logos/company-$companyId.$extension"

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

        val company = companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Empresa não encontrada") }

        company.logoUrl = url
        company.updateLogoAt = Instant.now()

        companyRepository.save(company)
    }
}
