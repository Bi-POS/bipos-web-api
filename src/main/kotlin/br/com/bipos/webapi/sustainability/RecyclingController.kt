package br.com.bipos.webapi.sustainability

import br.com.bipos.webapi.security.SecurityUtils
import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.sustainability.dto.RecyclingPointDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sustainability")
class RecyclingController(
    private val recyclingService: RecyclingService,
    private val companyRepository: CompanyRepository
) {

    @GetMapping("/recycling-points")
    fun getPoints(): ResponseEntity<List<RecyclingPointDTO>> {

        val companyId = SecurityUtils.getCompanyId()

        val company = companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Empresa não encontrada") }

        val lat = company.latitude
            ?: return ResponseEntity.badRequest().build()

        val lon = company.longitude
            ?: return ResponseEntity.badRequest().build()

        val points = recyclingService.getRecyclingPoints(lat, lon)

        return ResponseEntity.ok(points)
    }
}
