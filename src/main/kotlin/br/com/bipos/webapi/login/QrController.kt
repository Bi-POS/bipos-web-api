package br.com.bipos.webapi.login

import br.com.bipos.webapi.login.dto.QrRequest
import br.com.bipos.webapi.login.dto.QrResponse
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
        @RequestBody request: QrRequest,
        @AuthenticationPrincipal user: AppUserDetails
    ): QrResponse {

        return smartPosLoginTokenService.generateQrToken(
            userId = user.id.toString(),
            companyId = request.companyId
        )
    }
}