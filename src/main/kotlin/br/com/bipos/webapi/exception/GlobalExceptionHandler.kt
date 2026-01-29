package br.com.bipos.webapi.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            mapOf(
                "status" to 403,
                "message" to ex.message
            )
        ) as ResponseEntity<Map<String, Any>>
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Usuário não encontrado"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(UnauthorizedUserException::class)
    fun handleUnauthorized(ex: UnauthorizedUserException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "Usuário não autorizado"
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Erro interno do servidor"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
