package br.com.bipos.webapi.security

import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object SecurityUtils {

    fun getCompanyId(): UUID? {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("Usuário não autenticado")

        val principal = authentication.principal as AppUserDetails

        return principal.user.company?.id
    }
}
