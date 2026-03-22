package br.com.bipos.webapi.exception

import org.springframework.http.HttpStatus

open class ResourceNotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)
