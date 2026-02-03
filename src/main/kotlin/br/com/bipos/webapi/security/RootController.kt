package br.com.bipos.webapi.security

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @GetMapping("/")
    fun ok(): Map<String, String> =
        mapOf("status" to "UP")
}

@RestController
class ReadyController {

    @GetMapping("/ready")
    fun ready(): ResponseEntity<String> =
        ResponseEntity.ok("OK")
}