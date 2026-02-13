package br.com.bipos.webapi.sale

import br.com.bipos.webapi.companymodule.CompanyModuleRepository
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.module.ModuleRepository
import br.com.bipos.webapi.module.ModuleStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class SaleModuleService(
    private val companyModuleRepository: CompanyModuleRepository,
    private val moduleRepository: ModuleRepository
) {

    fun validateAccess(companyId: UUID?) {  // 🔥 Mudei para UUID (não nullable)
        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: throw IllegalStateException("Módulo SALE não cadastrado no sistema")

        val hasModuleActive = companyModuleRepository
            .existsByCompanyIdAndModuleAndEnabledTrue(companyId, saleModule)

        if (!hasModuleActive) {
            throw IllegalStateException(
                "Empresa não possui o módulo SALE ativo. " +
                        "Verifique se a empresa contratou e ativou o módulo."
            )
        }
    }

    fun checkModuleStatus(companyId: UUID): ModuleStatus {
        val saleModule = moduleRepository.findByName(ModuleType.SALE)
            ?: return ModuleStatus.NOT_FOUND

        val isEnabled = companyModuleRepository
            .findEnabledByCompanyIdAndModule(companyId, saleModule)

        return when (isEnabled) {
            true -> ModuleStatus.ACTIVE
            false -> ModuleStatus.INACTIVE
            null -> ModuleStatus.NOT_ASSIGNED
        }
    }
}
