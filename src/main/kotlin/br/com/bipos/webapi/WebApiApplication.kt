package br.com.bipos.webapi

import br.com.bipos.webapi.security.auth.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class WebApiApplication

fun main(args: Array<String>) {
    runApplication<WebApiApplication>(*args)
}
