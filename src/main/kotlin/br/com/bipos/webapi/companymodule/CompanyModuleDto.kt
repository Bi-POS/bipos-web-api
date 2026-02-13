package br.com.bipos.webapi.companymodule

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

data class CompanyModuleDto(
    @JsonProperty("id")
    val id: UUID,

    @JsonProperty("companyId")
    val companyId: UUID,

    @JsonProperty("moduleId")
    val moduleId: UUID,

    @JsonProperty("moduleName")
    val moduleName: String,

    @JsonProperty("moduleType")
    val moduleType: String,

    @JsonProperty("enabled")
    val enabled: Boolean,

    @JsonProperty("createdAt")
    val createdAt: LocalDateTime?,

    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime?,

    @JsonProperty("activatedAt")
    val activatedAt: LocalDateTime?,

    @JsonProperty("deactivatedAt")
    val deactivatedAt: LocalDateTime?
) {

    // Construtor secundário para facilitar a criação
    constructor(
        id: UUID,
        companyId: UUID,
        moduleId: UUID,
        moduleName: String,
        moduleType: String,
        enabled: Boolean
    ) : this(
        id = id,
        companyId = companyId,
        moduleId = moduleId,
        moduleName = moduleName,
        moduleType = moduleType,
        enabled = enabled,
        createdAt = null,
        updatedAt = null,
        activatedAt = null,
        deactivatedAt = null
    )
}