package br.com.bipos.webapi.exception

class InvalidQrTokenException(
    message: String = "Token inválido"
) : ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, message)
