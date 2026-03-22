package br.com.bipos.webapi.sustainability

import br.com.bipos.webapi.exception.InternalServerException
import br.com.bipos.webapi.sustainability.dto.RecyclingPointDTO
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RecyclingService(
    private val restTemplate: RestTemplate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RecyclingService::class.java)
    }

    /**
     * 🔁 Lista de servidores Overpass (fallback automático)
     */
    private val overpassServers = listOf(
        "https://overpass.kumi.systems/api/interpreter",
        "https://lz4.overpass-api.de/api/interpreter",
        "https://overpass-api.de/api/interpreter"
    )

    /**
     * 🌍 Busca pontos de reciclagem (cacheado por cidade)
     */
    @Cacheable(
        value = ["recyclingPoints"],
        key = "T(java.lang.String).format('%.5f-%.5f-5000', #lat, #lon)"
    )
    fun getRecyclingPoints(lat: Double, lon: Double): List<RecyclingPointDTO> {

        val query = """
        [out:json][timeout:25];
        (
          node["amenity"="recycling"](around:5000,$lat,$lon);
          way["amenity"="recycling"](around:5000,$lat,$lon);
          relation["amenity"="recycling"](around:5000,$lat,$lon);
        );
        out center tags;
    """.trimIndent()

        val response = callOverpass(query)

        val elements = response["elements"] as? List<Map<String, Any>> ?: emptyList()

        return elements
            .take(30)
            .mapNotNull {

                val elLat = when {
                    it["lat"] != null -> (it["lat"] as Number).toDouble()
                    it["center"] != null -> ((it["center"] as Map<*, *>)["lat"] as Number).toDouble()
                    else -> null
                }

                val elLon = when {
                    it["lon"] != null -> (it["lon"] as Number).toDouble()
                    it["center"] != null -> ((it["center"] as Map<*, *>)["lon"] as Number).toDouble()
                    else -> null
                }

                if (elLat == null || elLon == null) return@mapNotNull null

                val tags = it["tags"] as? Map<String, Any>

                RecyclingPointDTO(
                    name = tags?.get("name")?.toString() ?: "Ponto de Reciclagem",
                    address = buildAddressFromTags(tags) ?: "Próximo à sua localização",
                    latitude = elLat,
                    longitude = elLon,
                    materialTypes = extractMaterials(tags)
                )
            }
    }

    /**
     * 🔁 Chama Overpass com fallback entre servidores
     */
    private fun callOverpass(query: String): Map<*, *> {

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val entity = HttpEntity("data=$query", headers)

        for (server in overpassServers) {
            try {
                logger.debug("Trying Overpass server {}", server)

                val response = restTemplate.postForObject(server, entity, Map::class.java)

                if (response != null) return response

            } catch (ex: Exception) {
                logger.warn("Overpass server {} failed", server)
            }
        }

        throw InternalServerException("Nenhum servidor Overpass respondeu.")
    }

    /**
     * 📍 Reverse geocode (cacheado)
     */
    @Cacheable(value = ["reverseAddress"], key = "#lat.toString().concat('-').concat(#lon.toString())")
    fun resolveAddressCached(lat: Double, lon: Double): String {

        return try {
            logger.debug("Reverse geocoding coordinates {}, {}", lat, lon)

            val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=$lat&lon=$lon&format=json"

            val headers = HttpHeaders()
            headers.set("User-Agent", "bipos-app") // obrigatório

            val entity = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map::class.java
            ).body

            val address = response?.get("address") as? Map<*, *>

            formatAddress(address)

        } catch (ex: Exception) {
            "Localização aproximada"
        }
    }

    /**
     * 🏷️ Monta endereço limpo (evita aquele endereço gigante de Portugal 😂)
     */
    private fun formatAddress(address: Map<*, *>?): String {
        if (address == null) return "Endereço não informado"

        val road = address["road"]
        val suburb = address["suburb"]
        val city = address["city"] ?: address["town"]
        val state = address["state"]

        return listOfNotNull(road, suburb, city, state)
            .joinToString(", ")
            .ifBlank { "Endereço não informado" }
    }

    /**
     * ♻️ Extrai materiais aceitos
     */
    private fun extractMaterials(tags: Map<String, Any>?): List<String> {
        if (tags == null) return emptyList()

        val materialMap = mapOf(
            "recycling:glass" to "Vidro",
            "recycling:paper" to "Papel",
            "recycling:plastic" to "Plástico",
            "recycling:metal" to "Metal",
            "recycling:electronics" to "Eletrônicos",
            "recycling:clothes" to "Roupas",
            "recycling:batteries" to "Baterias"
        )

        return materialMap
            .filter { (key, _) -> tags[key]?.toString() == "yes" }
            .map { it.value }
    }

    private fun buildAddressFromTags(tags: Map<String, Any>?): String? {
        if (tags == null) return null

        val street = tags["addr:street"]?.toString()
        val number = tags["addr:housenumber"]?.toString()
        val city = tags["addr:city"]?.toString()
        val suburb = tags["addr:suburb"]?.toString()

        val address = listOfNotNull(street, number, suburb, city)
            .joinToString(", ")

        return if (address.isBlank()) null else address
    }

}
