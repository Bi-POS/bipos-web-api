package br.com.bipos.webapi.companymodule

import br.com.bipos.webapi.domain.companymodule.CompanyModule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import br.com.bipos.webapi.domain.module.Module

interface CompanyModuleRepository : JpaRepository<CompanyModule, UUID?> {

    fun existsByCompanyIdAndModule(
        companyId: UUID?,
        module: Module
    ): Boolean

    fun findAllByCompanyId(companyId: UUID?): List<CompanyModule>

    fun deleteAllByCompanyId(companyId: UUID)
}