package br.com.bipos.webapi.companymodule

import br.com.bipos.webapi.domain.companymodule.CompanyModule

fun CompanyModule.toDto(): CompanyModuleDto? {
    val moduleId = this.id ?: return null
    val companyIdValue = this.company?.id ?: return null
    val moduleModule = this.module ?: return null
    val moduleModuleId = moduleModule.id ?: return null

    return CompanyModuleDto(
        id = moduleId,
        companyId = companyIdValue,
        moduleId = moduleModuleId,
        moduleName = moduleModule.name.name,
        moduleType = moduleModule.name.name,
        enabled = this.enabled,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        activatedAt = this.activatedAt,
        deactivatedAt = this.deactivatedAt
    )
}