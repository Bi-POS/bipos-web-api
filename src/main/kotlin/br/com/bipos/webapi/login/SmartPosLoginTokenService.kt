package br.com.bipos.webapi.login

import br.com.bipos.webapi.domain.login.SmartPosLoginToken
import br.com.bipos.webapi.login.dto.QrResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

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

        // 1️⃣ Invalida QRs anteriores ainda ativos
        repository.invalidateActiveTokens(
            userId = userId,
            companyId = companyId,
            now = now
        )

        // 2️⃣ Cria novo token
        val token = UUID.randomUUID().toString()
        val expiresAt = now.plusSeconds(120)

        val entity = SmartPosLoginToken(
            token = token,
            userId = userId,
            companyId = companyId,
            expiresAt = expiresAt
        )

        repository.save(entity)

        // 3️⃣ Retorna payload do QR
        return QrResponse(
            token = token,
            expiresAt = expiresAt
        )
    }
}
