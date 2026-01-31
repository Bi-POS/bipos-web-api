package br.com.bipos.webapi.init

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "do.spaces")
data class SpacesProperties @ConstructorBinding constructor(
    val bucket: String,
    val cdn: String
)