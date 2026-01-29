package br.com.bipos.webapi.company.dto

import br.com.bipos.webapi.domain.utils.DocumentType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CompanyCreateDTO(

    @field:NotBlank(message = "Nome da empresa é obrigatório")
    val name: String,

    @field:Email(message = "Email da empresa inválido")
    val email: String,

    @field:NotBlank(message = "Documento é obrigatório")
    val document: String,

    val documentType: DocumentType = DocumentType.CNPJ,

    @field:NotBlank(message = "Telefone é obrigatório")
    val phone: String,

    val logoUrl: String? = null,

    // 🔥 DADOS DO OWNER
    @field:NotBlank(message = "Nome do responsável é obrigatório")
    val ownerName: String,

    @field:Email(message = "Email do responsável inválido")
    val ownerEmail: String,

    @field:Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    val ownerPassword: String
)
