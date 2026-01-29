package br.com.bipos.webapi.security.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt.web")
data class WebJwtProperties(
    val secret: String,
    val expiration: Long
)