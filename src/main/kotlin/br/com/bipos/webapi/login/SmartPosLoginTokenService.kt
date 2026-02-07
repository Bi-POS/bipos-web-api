package br.com.bipos.webapi.login

import br.com.bipos.webapi.domain.login.SmartPosLoginToken
import br.com.bipos.webapi.exception.InvalidQrTokenException
import br.com.bipos.webapi.login.dto.QrResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class SmartPosLoginTokenService(
    private val repository: SmartPosLoginTokenRepository
) {

    /**
     * Gera QR Code de login para SmartPOS
     * - invalida tokens antigos
     * - gera token único
     * - transacional
     */
    @Transactional
    fun generateQrToken(
        userId: String,
        companyId: String
    ): QrResponse {

        val now = Instant.now()

        repository.invalidateActiveTokens(
            userId = UUID.fromString(userId),
            companyId = UUID.fromString(companyId),
            now = now
        )

        val token = UUID.randomUUID().toString()
        val expiresAt = now.plusSeconds(120)

        repository.save(
            SmartPosLoginToken(
                token = token,
                userId = UUID.fromString(userId),
                companyId = UUID.fromString(companyId),
                expiresAt = expiresAt,
                used = false
            )
        )

        return QrResponse(
            token = token,
            expiresAt = expiresAt
        )
    }
}
