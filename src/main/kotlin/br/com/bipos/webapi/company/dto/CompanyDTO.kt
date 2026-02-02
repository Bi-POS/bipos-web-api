package br.com.bipos.webapi.company.dto

import br.com.bipos.webapi.domain.utils.DocumentType
import java.time.Instant
import java.util.*

data class CompanyDTO(
    val id: UUID?,
    val name: String,
    val email: String,
    val document: String,
    val documentType: DocumentType,
    val phone: String,
    val logoUrl: String?,
    val updateLogoAt: Instant,
    val status: String
)
