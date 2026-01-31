package br.com.bipos.webapi.init

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SpacesProperties::class)
class PropertiesConfig