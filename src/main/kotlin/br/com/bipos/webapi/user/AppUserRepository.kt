package br.com.bipos.webapi.user

import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.domain.user.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AppUserRepository : JpaRepository<AppUser, UUID> {
    fun findByEmail(email: String): AppUser?

    fun existsByEmail(email: String): Boolean

    fun findAllByCompanyId(companyId: UUID): List<AppUser>

    fun findByIdAndCompanyId(
        id: UUID,
        companyId: UUID
    ): AppUser?

    fun findAllByCompanyIdAndRoleNot(
        companyId: UUID,
        role: UserRole
    ): List<AppUser>
}