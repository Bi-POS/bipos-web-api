package br.com.bipos.webapi.security

import br.com.bipos.webapi.exception.AuthenticationRequiredException
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object SecurityUtils {

    fun getCurrentUser(): AppUserDetails {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationRequiredException()

        return authentication.principal as? AppUserDetails
            ?: throw AuthenticationRequiredException()
    }

    fun getCompanyId(): UUID =
        getCurrentUser().requireCompanyId()
}
