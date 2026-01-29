package br.com.bipos.webapi.sale.group

import br.com.bipos.webapi.sale.group.dto.SaleGroupCreateDTO
import br.com.bipos.webapi.sale.group.dto.SaleGroupDTO
import br.com.bipos.webapi.security.SecurityUtils
import br.com.bipos.webapi.domain.catalog.Group
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.sale.SaleModuleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.map

@Service
class SaleGroupService(
    private val saleGroupRepository: SaleGroupRepository,
    private val companyRepository: CompanyRepository,
    private val moduleRepository: ModuleRepository,
    private val saleModuleService: SaleModuleService
) {

    // =========================
    // CREATE
    // =========================
    @Transactional
    fun create(dto: SaleGroupCreateDTO): SaleGroupDTO {

        val companyId = SecurityUtils.getCompanyId()

        saleModuleService.validateAccess(companyId)

        val company = companyRepository.findById(companyId)
            .orElseThrow { IllegalArgumentException("Empresa não encontrada") }

        if (saleGroupRepository.existsByCompanyIdAndNameIgnoreCase(companyId, dto.name)) {
            throw IllegalArgumentException("Já existe um grupo com esse nome")
        }

        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: throw IllegalStateException("Módulo SALE não cadastrado")

        val group = Group(
            name = dto.name.trim(),
            imageUrl = dto.imageUrl,
            company = company,
            module = saleModule
        )

        val saved = saleGroupRepository.save(group)

        return SaleGroupDTO(
            id = saved.id,
            name = saved.name,
            imageUrl = saved.imageUrl
        )
    }

    // =========================
    // LIST
    // =========================
    fun list(): List<SaleGroupDTO> {
        val companyId = SecurityUtils.getCompanyId()

        return saleGroupRepository
            .findAllByCompanyId(companyId)
            .map {
                SaleGroupDTO(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            }
    }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    fun update(groupId: UUID, dto: SaleGroupCreateDTO): SaleGroupDTO {

        val companyId = SecurityUtils.getCompanyId()

        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw IllegalArgumentException("Grupo não encontrado")

        group.name = dto.name.trim()
        group.imageUrl = dto.imageUrl

        return SaleGroupDTO(
            id = group.id,
            name = group.name,
            imageUrl = group.imageUrl
        )
    }

    // =========================
    // DELETE
    // =========================
    @Transactional
    fun delete(groupId: UUID) {

        val companyId = SecurityUtils.getCompanyId()

        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw IllegalArgumentException("Grupo não encontrado")

        saleGroupRepository.delete(group)
    }

    fun getById(groupId: UUID): SaleGroupDTO {

        val companyId = SecurityUtils.getCompanyId()

        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw IllegalArgumentException("Grupo não encontrado")

        return SaleGroupDTO(
            id = group.id,
            name = group.name,
            imageUrl = group.imageUrl
        )
    }
}
