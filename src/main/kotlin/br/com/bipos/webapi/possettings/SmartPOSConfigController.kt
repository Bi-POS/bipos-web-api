// possettings/SmartPosSettingsController.kt
package br.com.bipos.webapi.possettings

import br.com.bipos.webapi.user.AppUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/smartpos/settings") // 🔥 Verifique se é exatamente isso
class SmartPosSettingsController(
    private val settingsService: SmartPosSettingsService
) {

    @GetMapping
    fun getSettings(
        @AuthenticationPrincipal userPrincipal: AppUserDetails
    ): ResponseEntity<SmartPosSettingsResponse> {
        println("🔵 GET /settings chamado para company: ${userPrincipal.user.company?.id}")

        val companyId = userPrincipal.user.company?.id
            ?: throw IllegalStateException("Usuário não está associado a uma empresa")

        val response = settingsService.getSettings(companyId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createOrUpdateSettings(
        @AuthenticationPrincipal userPrincipal: AppUserDetails,
        @Valid @RequestBody request: SmartPosSettingsRequest
    ): ResponseEntity<SmartPosSettingsResponse> {
        val companyId = userPrincipal.user.company?.id
            ?: throw IllegalStateException("Usuário não está associado a uma empresa")

        val response = settingsService.createOrUpdateSettings(companyId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}