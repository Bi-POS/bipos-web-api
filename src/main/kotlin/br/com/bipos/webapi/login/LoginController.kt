package br.com.bipos.webapi.login

import br.com.bipos.webapi.login.dto.LoginRequest
import br.com.bipos.webapi.security.auth.AuthService
import br.com.bipos.webapi.security.auth.response.AuthResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth", "/api/v1/auth")
class LoginController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<AuthResponse> {
        val token = authService.login(req.useremail, req.password)
        return ResponseEntity.ok(AuthResponse(token))
    }
}
