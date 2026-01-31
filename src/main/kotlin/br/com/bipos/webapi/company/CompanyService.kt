package br.com.bipos.webapi.company

import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.company.CompanyStatus
import br.com.bipos.webapi.domain.companymodule.CompanyModule
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import br.com.bipos.webapi.domain.utils.DocumentType
import br.com.bipos.webapi.company.dto.CompanyCreateDTO
import br.com.bipos.webapi.company.dto.CompanyDTO
import br.com.bipos.webapi.companymodule.CompanyModuleRepository
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.user.AppUserRepository
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
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
        id = company.id!!,
        name = company.name,
        email = company.email,
        document = company.document,
        documentType = company.documentType,
        phone = company.phone,
        logoUrl = null,
        status = company.status.name
    )

    fun updateLogo(companyId: UUID, file: MultipartFile) {

        if (file.isEmpty) {
            throw IllegalArgumentException("Arquivo inválido")
        }

        if (!file.contentType.orEmpty().startsWith("image")) {
            throw IllegalArgumentException("Apenas imagens são permitidas")
        }

        val extension = file.originalFilename
            ?.substringAfterLast(".", "png")

        val fileName = "company-$companyId.$extension"

        // 📁 PATH FÍSICO (onde o arquivo vai existir)
        Files.createDirectories(uploadBaseDir)
        val physicalPath = uploadBaseDir.resolve(fileName)

        Files.copy(
            file.inputStream,
            physicalPath,
            StandardCopyOption.REPLACE_EXISTING
        )

        val company = companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Empresa não encontrada") }

        // 🌍 PATH PÚBLICO (o que vai para o banco)
        company.logoUrl = "$publicBasePath/$fileName"

        companyRepository.save(company)
    }
}
