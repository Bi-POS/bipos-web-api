package br.com.bipos.webapi.possettings

import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.companymodule.CompanyModuleDto
import br.com.bipos.webapi.companymodule.toDto
import br.com.bipos.webapi.domain.settings.SmartPosPrint
import br.com.bipos.webapi.domain.settings.SmartPosSaleOperationMode
import br.com.bipos.webapi.domain.settings.SmartPosSettings
import br.com.bipos.webapi.exception.BusinessException
import br.com.bipos.webapi.exception.InternalServerException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class SmartPosSettingsService(
    private val repository: SmartPosSettingsRepository,
    private val companyRepository: CompanyRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    companion object {
        private val logger = LoggerFactory.getLogger(SmartPosSettingsService::class.java)
    }

    @Transactional
    fun createOrUpdateSettings(
        companyId: UUID,
        request: SmartPosSettingsRequest
    ): SmartPosSettingsResponse {
        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        validateRequest(request)

        val settings = repository.findByCompanyIdAndIsActiveTrue(companyId)
            .orElseGet { SmartPosSettings(companyId = companyId) }

        settings.saleOperationMode = SmartPosSaleOperationMode.fromString(request.saleOperationMode)
        settings.print = SmartPosPrint.fromString(request.print)
        settings.printLogo = request.printLogo
        settings.logoUrl = when {
            request.printLogo && !request.logoUrl.isNullOrBlank() -> request.logoUrl
            request.printLogo -> throw BusinessException("URL da logo é obrigatória quando printLogo é true")
            else -> null
        }

        settings.autoLogoutMinutes = request.autoLogoutMinutes
        settings.darkMode = request.darkMode
        settings.soundEnabled = request.soundEnabled

        updateSecurity(settings, request.security)

        settings.updatedAt = LocalDateTime.now()
        settings.version = settings.version + 1

        val saved = repository.save(settings)
        val companyModules = company.modules.mapNotNull { it.toDto() }

        return toResponse(saved, companyModules)
    }

    private fun updateSecurity(settings: SmartPosSettings, security: SmartPosSecurityRequest?) {
        when {
            security == null -> return
            !security.enabled -> {
                settings.securityEnabled = false
                settings.pinHash = null
                settings.pinAttempts = 0
                settings.lastPinChange = null
            }
            else -> {
                settings.securityEnabled = true

                if (security.pin.isNullOrBlank()) {
                    throw BusinessException("PIN é obrigatório quando segurança está ativada")
                }

                if (!security.pin.matches(Regex("\\d{4,6}"))) {
                    throw BusinessException("PIN deve ter entre 4 e 6 dígitos numéricos")
                }

                settings.updatePin(passwordEncoder.encode(security.pin))
            }
        }
    }

    private fun validateRequest(request: SmartPosSettingsRequest) {
        if (request.autoLogoutMinutes !in 1..60) {
            throw BusinessException("Tempo de logout deve estar entre 1 e 60 minutos")
        }
    }

    @Transactional(readOnly = true)
    fun getSettings(companyId: UUID): SmartPosSettingsResponse {
        logger.debug("Fetching SmartPOS settings for company {}", companyId)

        val settings = repository.findByCompanyIdAndIsActiveTrue(companyId)
            .orElseThrow {
                logger.warn("SmartPOS settings not found for company {}", companyId)
                ResourceNotFoundException("Configurações não encontradas para a empresa")
            }

        logger.debug("SmartPOS settings found with id {}", settings.id)

        val company = companyRepository.findById(companyId)
            .orElseThrow {
                logger.warn("Company {} not found while fetching SmartPOS settings", companyId)
                ResourceNotFoundException("Empresa não encontrada")
            }

        logger.debug("Company {} found for SmartPOS settings", company.name)

        val companyModules = company.modules.mapNotNull { it.toDto() }
        logger.debug("Loaded {} company modules for SmartPOS settings", companyModules.size)

        return toResponse(settings, companyModules)
    }

    private fun toResponse(
        settings: SmartPosSettings,
        companyModules: List<CompanyModuleDto>
    ): SmartPosSettingsResponse {
        val settingsId = settings.id ?: throw InternalServerException("Settings ID não pode ser nulo")

        return SmartPosSettingsResponse(
            id = settingsId,
            saleOperationMode = settings.saleOperationMode.name,
            print = settings.print.name,
            printLogo = settings.printLogo,
            logoUrl = settings.logoUrl,
            securityEnabled = settings.securityEnabled,
            hasPin = settings.pinHash != null,
            lastPinChange = settings.lastPinChange,
            pinAttempts = settings.pinAttempts,
            autoLogoutMinutes = settings.autoLogoutMinutes,
            darkMode = settings.darkMode,
            soundEnabled = settings.soundEnabled,
            availableModules = companyModules,
            version = settings.version,
            updatedAt = settings.updatedAt
        )
    }
}
