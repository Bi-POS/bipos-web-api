package br.com.bipos.webapi.possettings

import br.com.bipos.webapi.companymodule.CompanyModuleDto
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

data class SmartPosSettingsResponse(
    @JsonProperty("id")
    val id: UUID,

    @JsonProperty("saleOperationMode")
    val saleOperationMode: String,

    @JsonProperty("print")
    val print: String,

    @JsonProperty("printLogo")
    val printLogo: Boolean,

    @JsonProperty("logoUrl")
    val logoUrl: String?,

    @JsonProperty("securityEnabled")
    val securityEnabled: Boolean,

    @JsonProperty("hasPin")
    val hasPin: Boolean,

    @JsonProperty("lastPinChange")
    val lastPinChange: LocalDateTime?,

    @JsonProperty("pinAttempts")
    val pinAttempts: Int,

    @JsonProperty("autoLogoutMinutes")
    val autoLogoutMinutes: Int,

    @JsonProperty("darkMode")
    val darkMode: Boolean,

    @JsonProperty("soundEnabled")
    val soundEnabled: Boolean,

    @JsonProperty("availableModules")
    val availableModules: List<CompanyModuleDto>,

    @JsonProperty("version")
    val version: Long,

    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime
)
