package br.com.bipos.webapi.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@ControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        ex: ApiException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val error = buildError(
            status = ex.status,
            message = ex.message,
            request = request
        )
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val error = buildError(
            status = status,
            message = ex.reason ?: "Erro na requisição",
            request = request
        )
        return ResponseEntity.status(status).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val details = ex.bindingResult
            .allErrors
            .mapNotNull { error ->
                when (error) {
                    is FieldError -> "${error.field}: ${error.defaultMessage}"
                    else -> error.defaultMessage
                }
            }

        val body = buildError(
            status = HttpStatus.BAD_REQUEST,
            message = "Dados de requisição inválidos",
            request = request,
            details = details
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val details = ex.constraintViolations
            .map { violation -> "${violation.propertyPath}: ${violation.message}" }

        val body = buildError(
            status = HttpStatus.BAD_REQUEST,
            message = "Parâmetros inválidos",
            request = request,
            details = details
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val body = buildError(
            status = HttpStatus.BAD_REQUEST,
            message = "Corpo da requisição inválido",
            request = request
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val body = buildError(
            status = HttpStatus.BAD_REQUEST,
            message = "Parâmetro '${ex.name}' inválido",
            request = request
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val body = buildError(
            status = HttpStatus.FORBIDDEN,
            message = ex.message ?: "Acesso negado",
            request = request
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("Unhandled exception on {} {}", request.method, request.requestURI, ex)

        val body = buildError(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Erro interno do servidor",
            request = request
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    private fun buildError(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
        details: List<String> = emptyList()
    ) = ApiError(
        status = status.value(),
        error = status.reasonPhrase,
        message = message,
        path = request.requestURI,
        details = details
    )
}
