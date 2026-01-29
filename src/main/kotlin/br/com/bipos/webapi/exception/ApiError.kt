package br.com.bipos.webapi.exception

data class ApiError(
    val status: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)