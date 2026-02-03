package br.com.bipos.webapi.login

import br.com.bipos.webapi.login.dto.QrRequest
import br.com.bipos.webapi.login.dto.QrResponse
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class QrController(
    private val smartPosLoginTokenService: SmartPosLoginTokenService
) {


    @PostMapping("/smartpos/qrcode")
    fun generateQr(
        @Valid @RequestBody request: QrRequest,
        authentication: Authentication
    ): QrResponse {

        val userId = authentication.principal as String

        return smartPosLoginTokenService.generateQrToken(
            userId = userId,
            companyId = request.companyId
        )
    }
}