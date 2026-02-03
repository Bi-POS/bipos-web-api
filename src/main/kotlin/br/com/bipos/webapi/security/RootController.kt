package br.com.bipos.webapi.security

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @GetMapping("/")
    fun ok(): Map<String, String> =
        mapOf("status" to "UP")
}