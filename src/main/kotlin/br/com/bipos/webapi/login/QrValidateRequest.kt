package br.com.bipos.webapi.login

data class QrValidateRequest(
    val token: String,
    val serialNumber: String
)