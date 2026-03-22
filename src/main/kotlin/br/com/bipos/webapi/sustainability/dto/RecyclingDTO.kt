
package br.com.bipos.webapi.sustainability.dto

data class RecyclingPointDTO(
    val name: String,
    val address: String,
    val materialTypes: List<String>,
    val latitude: Double?,
    val longitude: Double?
)
