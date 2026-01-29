package br.com.bipos.webapi.company

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import br.com.bipos.webapi.domain.company.Company

interface CompanyRepository : JpaRepository<Company, UUID> {

    fun findByEmail(email: String): Company?

    fun existsByEmail(email: String): Boolean

    fun existsByDocument(document: String): Boolean
}
