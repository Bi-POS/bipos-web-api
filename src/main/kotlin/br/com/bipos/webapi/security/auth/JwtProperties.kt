package br.com.bipos.webapi.security.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt.web")
data class JwtProperties(
    val secret: String,
    val expiration: Long
)