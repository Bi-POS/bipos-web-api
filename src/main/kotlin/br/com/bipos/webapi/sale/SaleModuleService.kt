package br.com.bipos.webapi.sale

import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.companymodule.CompanyModuleRepository
import br.com.bipos.webapi.module.ModuleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class SaleModuleService(
    private val companyModuleRepository: CompanyModuleRepository,
    private val moduleRepository: ModuleRepository
) {

    fun validateAccess(companyId: UUID?) {
        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: throw IllegalStateException("Módulo SALE não cadastrado")

        val hasModule = companyModuleRepository
            .existsByCompanyIdAndModule(companyId, saleModule)

        if (!hasModule) {
            throw IllegalStateException("Empresa não possui o módulo SALE")
        }
    }
}
