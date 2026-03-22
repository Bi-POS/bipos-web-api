package br.com.bipos.webapi.exception

import org.springframework.http.HttpStatus

class UnauthorizedUserException(message: String) : ApiException(HttpStatus.UNAUTHORIZED, message)
