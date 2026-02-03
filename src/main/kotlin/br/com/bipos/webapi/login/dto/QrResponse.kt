package br.com.bipos.webapi.login.dto

import java.time.Instant

data class QrResponse(
    val type: String = "SMARTPOS_LOGIN",
    val token: String,
    val expiresAt: Instant
)