package br.com.bipos.webapi.init

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "do.spaces")
@Configuration
data class SpacesProperties(
    val bucket: String,
    val cdn: String
)