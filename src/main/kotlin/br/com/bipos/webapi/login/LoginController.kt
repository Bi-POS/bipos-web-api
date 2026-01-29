package br.com.bipos.webapi.login

import br.com.bipos.webapi.security.auth.AuthService
import br.com.bipos.webapi.security.auth.response.AuthResponse
import br.com.bipos.webapi.exception.UnauthorizedUserException
import br.com.bipos.webapi.login.request.LoginRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/auth")
class LoginController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<AuthResponse> {
        val token = authService.login(req.useremail, req.password)
            ?: throw UnauthorizedUserException("Credenciais inválidas para o usuário '${req.useremail}'")
        return ResponseEntity.ok(AuthResponse(token))
    }
}