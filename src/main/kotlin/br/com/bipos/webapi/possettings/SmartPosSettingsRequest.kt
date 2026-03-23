package br.com.bipos.webapi.possettings

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SmartPosSettingsRequest(
    @field:NotNull(message = "Tipo de operação é obrigatório")
    @JsonProperty("saleOperationMode")
    val saleOperationMode: String = "DIRECT",

    @field:NotNull(message = "Tipo de impressão é obrigatório")
    @JsonProperty("print")
    val print: String,

    @JsonProperty("printLogo")
    val printLogo: Boolean = false,

    @JsonProperty("logoUrl")
    val logoUrl: String? = null,

    @JsonProperty("security")
    val security: SmartPosSecurityRequest? = null,

    @JsonProperty("autoLogoutMinutes")
    @field:Min(1, message = "Tempo mínimo de logout é 1 minuto")
    @field:Max(60, message = "Tempo máximo de logout é 60 minutos")
    val autoLogoutMinutes: Int = 5,

    @JsonProperty("darkMode")
    val darkMode: Boolean = false,

    @JsonProperty("soundEnabled")
    val soundEnabled: Boolean = true
)

data class SmartPosSecurityRequest(
    val enabled: Boolean = false,

    @field:Size(min = 4, max = 6, message = "PIN deve ter entre 4 e 6 dígitos")
    @field:Pattern(regexp = "\\d+", message = "PIN deve conter apenas números")
    val pin: String? = null
)
