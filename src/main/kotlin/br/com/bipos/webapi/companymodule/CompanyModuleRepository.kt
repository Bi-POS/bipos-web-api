package br.com.bipos.webapi.companymodule

import br.com.bipos.webapi.domain.companymodule.CompanyModule
import br.com.bipos.webapi.domain.module.Module
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface CompanyModuleRepository : JpaRepository<CompanyModule, UUID> {

    fun existsByCompanyIdAndModuleAndEnabledTrue(
        companyId: UUID?,
        module: Module
    ): Boolean

    @Deprecated("Use existsByCompanyIdAndModuleAndEnabledTrue para verificar se está ativo")
    fun existsByCompanyIdAndModule(
        companyId: UUID?,
        module: Module
    ): Boolean

    fun findAllByCompanyIdAndEnabledTrue(companyId: UUID): List<CompanyModule>

    fun findAllByCompanyId(companyId: UUID?): List<CompanyModule>

    @Query("SELECT cm.enabled FROM CompanyModule cm WHERE cm.company.id = :companyId AND cm.module = :module")
    fun findEnabledByCompanyIdAndModule(
        @Param("companyId") companyId: UUID,
        @Param("module") module: Module
    ): Boolean?

    fun deleteAllByCompanyId(companyId: UUID)
}