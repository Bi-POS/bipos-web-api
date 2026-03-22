package br.com.bipos.webapi.exception

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
