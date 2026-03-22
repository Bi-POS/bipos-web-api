package br.com.bipos.webapi.login

import br.com.bipos.webapi.exception.ForbiddenOperationException
import br.com.bipos.webapi.login.dto.QrRequest
import br.com.bipos.webapi.login.dto.QrResponse
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.security.requireUserId
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth", "/api/v1/auth")
class QrController(
    private val smartPosLoginTokenService: SmartPosLoginTokenService
) {


    @PostMapping("/smartpos/qrcode")
    fun generateQr(
        @RequestBody request: QrRequest,
        @CurrentUser user: AppUserDetails
    ): QrResponse {
        val companyId = user.requireCompanyId()

        if (request.companyId != companyId.toString()) {
            throw ForbiddenOperationException("Não é permitido gerar QR code para outra empresa")
        }

        return smartPosLoginTokenService.generateQrToken(
            userId = user.requireUserId().toString(),
            companyId = companyId.toString()
        )
    }
}
