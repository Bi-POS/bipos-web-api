package br.com.bipos.webapi.exception

import org.springframework.http.HttpStatus

open class BusinessException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)
