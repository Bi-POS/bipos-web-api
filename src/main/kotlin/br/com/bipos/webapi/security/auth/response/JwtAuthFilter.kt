package br.com.bipos.webapi.security.auth.response

import br.com.bipos.webapi.domain.user.AppUser
import br.com.bipos.webapi.security.auth.JwtService
import br.com.bipos.webapi.user.AppUserDetails
import br.com.bipos.webapi.user.AppUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: AppUserDetailsService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        return (
                path == "/auth/login" ||
                        path == "/auth/refresh" ||
                        path.startsWith("/uploads") ||
                        (request.method == "POST" && path == "/companies") ||
                        (request.method == "GET" && path.startsWith("/companies/") && path.endsWith("/logo")) ||
                        (request.method == "GET" && path.startsWith("/users/") && path.endsWith("/photo")) ||
                        request.method == "OPTIONS"
                )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(7)
            val username = jwtService.extractUsername(token)

            if (
                username != null &&
                SecurityContextHolder.getContext().authentication == null
            ) {
                val claims = jwtService.extractAllClaims(token)

                val userDetails =
                    userDetailsService.loadUserByUsername(username) as AppUserDetails

                if (jwtService.isTokenValid(token, userDetails)) {

                    val domainUser = userDetails.getDomainUser()

                    val enrichedUser =
                        if (domainUser.name.isNullOrBlank()) {
                            AppUser(
                                id = domainUser.id,
                                name = claims["userName"] as? String,
                                email = domainUser.email,
                                passwordHash = domainUser.passwordHash,
                                role = domainUser.role,
                                active = domainUser.active,
                                company = domainUser.company,
                                createdAt = domainUser.createdAt
                            )
                        } else {
                            domainUser
                        }

                    val enrichedDetails = AppUserDetails(enrichedUser)

                    val authToken = UsernamePasswordAuthenticationToken(
                        enrichedDetails,
                        null,
                        enrichedDetails.authorities
                    )

                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {
            SecurityContextHolder.clearContext()
            logger.warn("JWT inválido ou erro de autenticação", e)
        }

        filterChain.doFilter(request, response)
    }
}
