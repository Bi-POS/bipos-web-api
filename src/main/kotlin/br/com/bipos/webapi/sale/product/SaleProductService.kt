package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.sale.group.SaleGroupRepository
import br.com.bipos.webapi.sale.product.dto.SaleProductCreateDTO
import br.com.bipos.webapi.sale.product.dto.SaleProductDTO
import br.com.bipos.webapi.domain.catalog.Product
import br.com.bipos.webapi.sale.SaleModuleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
import kotlin.collections.map


@Service
class SaleProductService(
    private val saleProductRepository: SaleProductRepository,
    private val saleGroupRepository: SaleGroupRepository,
    private val saleModuleService: SaleModuleService,
) {

    // =========================
    // CREATE
    // =========================
    @Transactional
    fun create(companyId: UUID?, groupId: UUID, dto: SaleProductCreateDTO): SaleProductDTO {

        saleModuleService.validateAccess(companyId)

        val group = saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw IllegalArgumentException("Grupo não encontrado")

        if (dto.price <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Preço deve ser maior que zero")
        }

        if (saleProductRepository.existsByGroupIdAndNameIgnoreCase(groupId, dto.name)) {
            throw IllegalArgumentException("Já existe um produto com esse nome neste grupo")
        }

        val product = Product(
            name = dto.name.trim(),
            price = dto.price,
            unitType = dto.unitType,
            imageUrl = dto.imageUrl,
            group = group
        )

        val saved = saleProductRepository.save(product)

        return SaleProductDTO(
            id = saved.id,
            name = saved.name,
            price = saved.price,
            unitType = saved.unitType,
            imageUrl = saved.imageUrl,
            groupId = saved.group.id
        )
    }


    // =========================
    // LIST
    // =========================
    fun list(companyId: UUID?, groupId: UUID): List<SaleProductDTO> {

        saleGroupRepository.findByIdAndCompanyId(groupId, companyId)
            ?: throw IllegalArgumentException("Grupo não encontrado")

        return saleProductRepository
            .findAllByGroupId(groupId)
            .map {
                SaleProductDTO(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    unitType = it.unitType,
                    imageUrl = it.imageUrl,
                    groupId = it.group.id
                )
            }
    }

    fun listAll(companyId: UUID?): List<SaleProductDTO> =
        saleProductRepository
            .findAllByCompanyId(companyId)
            .map {
                SaleProductDTO(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    unitType = it.unitType,
                    imageUrl = it.imageUrl,
                    groupId = it.group.id
                )
            }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    fun update(
        companyId: UUID?,
        groupId: UUID,
        productId: UUID,
        dto: SaleProductCreateDTO
    ): SaleProductDTO {

        val product = saleProductRepository.findByIdAndGroupId(productId, groupId)
            ?: throw IllegalArgumentException("Produto não encontrado")

        if (dto.price <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Preço deve ser maior que zero")
        }

        product.name = dto.name.trim()
        product.price = dto.price
        product.unitType = dto.unitType
        product.imageUrl = dto.imageUrl

        return SaleProductDTO(
            id = product.id,
            name = product.name,
            price = product.price,
            unitType = product.unitType,
            imageUrl = product.imageUrl,
            groupId = product.group.id
        )
    }

    // =========================
    // DELETE
    // =========================
    @Transactional
    fun delete(
        companyId: UUID?,
        groupId: UUID,
        productId: UUID
    ) {
        val product = saleProductRepository
            .findByIdAndGroupIdAndGroupCompanyId(productId, groupId, companyId)
            ?: throw IllegalArgumentException("Produto não encontrado")

        saleProductRepository.delete(product)
    }
}
