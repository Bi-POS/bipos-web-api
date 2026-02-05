package br.com.bipos.webapi.security.auth

data class AuthenticatedUser(
    val id: String,
    val name: String,
    val email: String?
)