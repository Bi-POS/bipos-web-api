package br.com.bipos.webapi.security

import br.com.bipos.webapi.security.auth.response.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
open class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    open fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    open fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }

            .sessionManagement {
                it.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                )
            }

            .authorizeHttpRequests { auth ->

                auth.requestMatchers(HttpMethod.POST,"/auth/smartpos/qrcode").permitAll()

                auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                auth.requestMatchers("/uploads/**").permitAll()

                auth.requestMatchers(HttpMethod.POST, "/companies").permitAll()

                auth.requestMatchers(HttpMethod.GET, "/companies/*/logo").permitAll()

                auth.requestMatchers(HttpMethod.GET, "/users/*/photo").permitAll()

                auth.anyRequest().authenticated()
            }

        http.addFilterBefore(
            jwtAuthFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        http.headers { headers ->
            headers.frameOptions { it.disable() }
        }

        return http.build()
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:5173"
        )

        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        )

        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

}
