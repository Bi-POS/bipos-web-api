package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.domain.catalog.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface SaleProductRepository : JpaRepository<Product, UUID> {

    fun existsByGroupIdAndNameIgnoreCase(
        groupId: UUID,
        name: String
    ): Boolean

    fun findAllByGroupId(groupId: UUID): List<Product>

    fun findByIdAndGroupId(
        id: UUID,
        groupId: UUID
    ): Product?

    fun findByIdAndGroupIdAndGroupCompanyId(
        id: UUID,
        groupId: UUID,
        companyId: UUID?
    ): Product?

    @Query(
        """
        select p from Product p
        where p.group.company.id = :companyId
    """
    )
    fun findAllByCompanyId(companyId: UUID?): List<Product>
}
