package br.com.bipos.webapi.sale.group

import br.com.bipos.webapi.domain.catalog.Group
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SaleGroupRepository : JpaRepository<Group, UUID?> {

    fun existsByCompanyIdAndNameIgnoreCase(
        companyId: UUID?,
        name: String
    ): Boolean

    fun findAllByCompanyId(companyId: UUID?): List<Group>

    fun findByIdAndCompanyId(
        id: UUID?,
        companyId: UUID?
    ): Group?
}
