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
import br.com.bipos.webapi.exception.BusinessException
import br.com.bipos.webapi.exception.ConflictException
import br.com.bipos.webapi.exception.InternalServerException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.storage.SpacesStorageService
import br.com.bipos.webapi.user.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val moduleRepository: ModuleRepository,
    private val companyModuleRepository: CompanyModuleRepository,
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val storageService: SpacesStorageService
) {

    @Transactional
    fun create(dto: CompanyCreateDTO): CompanyDTO {

        if (companyRepository.existsByEmail(dto.email)) {
            throw ConflictException("Email já cadastrado")
        }

        if (companyRepository.existsByDocument(dto.document)) {
            throw ConflictException("Documento já cadastrado")
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
            ?: throw InternalServerException("Módulo SALE não cadastrado")

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
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }
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
        status = company.status.name,
        city = company.city,
        state = company.state,
        address = company.address,
        latitude = company.latitude,
        longitude = company.longitude
    )
    
    fun updateLogo(companyId: UUID, file: MultipartFile) {

        if (file.isEmpty || !file.contentType.orEmpty().startsWith("image")) {
            throw BusinessException("Arquivo inválido")
        }

        val extension = file.originalFilename?.substringAfterLast(".", "png")
        val key = "logos/company-$companyId.$extension"
        val url = storageService.uploadPublicFile(key, file)

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        company.logoUrl = url
        company.updateLogoAt = Instant.now()

        companyRepository.save(company)
    }
}
