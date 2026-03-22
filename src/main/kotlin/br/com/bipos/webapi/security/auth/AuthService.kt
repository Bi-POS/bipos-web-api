package br.com.bipos.webapi.security.auth

import br.com.bipos.webapi.exception.ForbiddenOperationException
import br.com.bipos.webapi.exception.InvalidCredentialsException
import br.com.bipos.webapi.user.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val appUserRepository: AppUserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    fun login(email: String, password: String): String {
        val user = appUserRepository.findByEmail(email)
            ?: throw InvalidCredentialsException()

        if (!user.active) {
            throw ForbiddenOperationException("Usuário desativado")
        }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return jwtService.generateToken(user)
    }
}
