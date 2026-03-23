// possettings/SmartPosSettingsController.kt
package br.com.bipos.webapi.possettings

import br.com.bipos.webapi.audit.toOperationalAuditActor
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/smartpos/settings", "/api/v1/smartpos/settings", "/api/v1/pos/admin/settings")
class SmartPosSettingsController(
    private val settingsService: SmartPosSettingsService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(SmartPosSettingsController::class.java)
    }

    @GetMapping
    fun getSettings(
        @CurrentUser userPrincipal: AppUserDetails
    ): ResponseEntity<SmartPosSettingsResponse> {
        val companyId = userPrincipal.requireCompanyId()
        logger.debug("Fetching SmartPOS settings for company {}", companyId)

        val response = settingsService.getSettings(companyId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createOrUpdateSettings(
        @CurrentUser userPrincipal: AppUserDetails,
        @Valid @RequestBody request: SmartPosSettingsRequest
    ): ResponseEntity<SmartPosSettingsResponse> {
        val companyId = userPrincipal.requireCompanyId()

        val response = settingsService.createOrUpdateSettings(
            companyId = companyId,
            request = request,
            actor = userPrincipal.toOperationalAuditActor()
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping
    fun replaceSettings(
        @CurrentUser userPrincipal: AppUserDetails,
        @Valid @RequestBody request: SmartPosSettingsRequest
    ): ResponseEntity<SmartPosSettingsResponse> {
        val companyId = userPrincipal.requireCompanyId()
        val response = settingsService.createOrUpdateSettings(
            companyId = companyId,
            request = request,
            actor = userPrincipal.toOperationalAuditActor()
        )
        return ResponseEntity.ok(response)
    }
}
