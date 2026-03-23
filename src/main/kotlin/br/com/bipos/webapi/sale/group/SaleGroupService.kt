package br.com.bipos.webapi.sale.group

import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.domain.catalog.Group
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.exception.ConflictException
import br.com.bipos.webapi.exception.InternalServerException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.sale.SaleModuleService
import br.com.bipos.webapi.sale.group.dto.SaleGroupCreateDTO
import br.com.bipos.webapi.sale.group.dto.SaleGroupDTO
import br.com.bipos.webapi.sale.product.SaleProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.map

@Service
class SaleGroupService(
    private val saleGroupRepository: SaleGroupRepository,
    private val companyRepository: CompanyRepository,
    private val moduleRepository: ModuleRepository,
    private val saleModuleService: SaleModuleService,
    private val saleProductRepository: SaleProductRepository,
) {

    @Transactional
    fun create(companyId: UUID, dto: SaleGroupCreateDTO): SaleGroupDTO {
        saleModuleService.validateAccess(companyId)

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        if (saleGroupRepository.existsByCompanyIdAndNameIgnoreCase(companyId, dto.name)) {
            throw ConflictException("Já existe um grupo com esse nome")
        }

        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: throw InternalServerException("Módulo SALE não cadastrado")

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

    fun list(companyId: UUID): List<SaleGroupDTO> =
        saleGroupRepository
            .findAllByCompanyId(companyId)
            .map {
                SaleGroupDTO(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            }

    @Transactional
    fun update(companyId: UUID, groupId: UUID, dto: SaleGroupCreateDTO): SaleGroupDTO {
        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw ResourceNotFoundException("Grupo não encontrado")

        group.name = dto.name.trim()
        group.imageUrl = dto.imageUrl

        return SaleGroupDTO(
            id = group.id,
            name = group.name,
            imageUrl = group.imageUrl
        )
    }

    @Transactional
    fun delete(companyId: UUID, groupId: UUID) {
        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw ResourceNotFoundException("Grupo não encontrado")

        if (saleProductRepository.existsByGroupId(groupId)) {
            throw ConflictException(
                "Não é possível excluir o grupo enquanto existirem produtos vinculados a ele. Exclua os produtos primeiro."
            )
        }

        saleGroupRepository.delete(group)
        saleGroupRepository.flush()
    }

    fun getById(companyId: UUID, groupId: UUID): SaleGroupDTO {
        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw ResourceNotFoundException("Grupo não encontrado")

        return SaleGroupDTO(
            id = group.id,
            name = group.name,
            imageUrl = group.imageUrl
        )
    }
}
