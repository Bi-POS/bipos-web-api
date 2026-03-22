package br.com.bipos.webapi.sustainability

import br.com.bipos.webapi.company.CompanyRepository
import br.com.bipos.webapi.exception.BusinessException
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.sustainability.dto.RecyclingPointDTO
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sustainability")
class RecyclingController(
    private val recyclingService: RecyclingService,
    private val companyRepository: CompanyRepository
) {

    @GetMapping("/recycling-points")
    fun getPoints(@CurrentUser user: AppUserDetails): ResponseEntity<List<RecyclingPointDTO>> {
        val company = companyRepository.findById(user.requireCompanyId())
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        val lat = company.latitude
            ?: throw BusinessException("Empresa sem latitude configurada")

        val lon = company.longitude
            ?: throw BusinessException("Empresa sem longitude configurada")

        val points = recyclingService.getRecyclingPoints(lat, lon)

        return ResponseEntity.ok(points)
    }
}
