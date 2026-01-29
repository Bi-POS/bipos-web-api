package br.com.bipos.webapi

import br.com.bipos.webapi.security.auth.WebJwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EntityScan("br.com.bipos.webapi.domain")
@EnableConfigurationProperties(WebJwtProperties::class)
class WebApiApplication

fun main(args: Array<String>) {
    runApplication<WebApiApplication>(*args)
}
