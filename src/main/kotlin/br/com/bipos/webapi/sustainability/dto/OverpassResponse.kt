package br.com.bipos.webapi.sustainability.dto

data class OverpassResponse(
    val elements: List<Element> = emptyList()
)

data class Element(
    val lat: Double?,
    val lon: Double?,
    val tags: Map<String, String> = emptyMap()
)
