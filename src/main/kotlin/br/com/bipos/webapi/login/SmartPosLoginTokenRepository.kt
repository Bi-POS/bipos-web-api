package br.com.bipos.webapi.login

import br.com.bipos.webapi.domain.login.SmartPosLoginToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface SmartPosLoginTokenRepository :
    JpaRepository<SmartPosLoginToken, String> {

    fun findByTokenAndUsedFalse(token: String): SmartPosLoginToken?

    fun deleteByExpiresAtBefore(now: Instant)
    @Modifying
    @Query("""
        update SmartPosLoginToken t
           set t.used = true
         where t.userId = :userId
           and t.companyId = :companyId
           and t.used = false
           and t.expiresAt > :now
    """)
    fun invalidateActiveTokens(
        userId: String,
        companyId: String,
        now: Instant
    )
}
