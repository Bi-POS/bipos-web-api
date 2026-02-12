package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.sale.product.dto.SaleProductCreateDTO
import br.com.bipos.webapi.sale.product.dto.SaleProductDTO
import br.com.bipos.webapi.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/sale")
class SaleProductController(
    private val saleProductService: SaleProductService
) {

    @GetMapping("/products/all")
    fun getAllProductsByCompany(): ResponseEntity<List<Map<String, Any>>> {
        val companyId = SecurityUtils.getCompanyId()

        val products = saleProductService.listAll(companyId)

        val response = products.map { productDTO ->
            mapOf(
                "productId" to productDTO.id,
                "productName" to productDTO.name,
                "price" to productDTO.price,
                "unitType" to productDTO.unitType.name,
                "imageUrl" to productDTO.imageUrl,
                "groupId" to productDTO.groupId
            )
        }

        return ResponseEntity.ok(response) as ResponseEntity<List<Map<String, Any>>>
    }

    // ============= ENDPOINTS EXISTENTES =============
    @PostMapping("/groups/{groupId}/products")
    fun create(
        @PathVariable groupId: UUID,
        @Valid @RequestBody dto: SaleProductCreateDTO
    ): ResponseEntity<SaleProductDTO> = ResponseEntity.status(HttpStatus.CREATED)
        .body(saleProductService.create(SecurityUtils.getCompanyId(), groupId, dto))

    @GetMapping("/groups/{groupId}/products")
    fun list(
        @PathVariable groupId: UUID
    ): List<SaleProductDTO> = saleProductService.list(SecurityUtils.getCompanyId(), groupId)

    @PutMapping("/groups/{groupId}/products/{productId}")
    fun update(
        @PathVariable groupId: UUID,
        @PathVariable productId: UUID,
        @Valid @RequestBody dto: SaleProductCreateDTO
    ): SaleProductDTO = saleProductService.update(SecurityUtils.getCompanyId(), groupId, productId, dto)

    @DeleteMapping("/groups/{groupId}/products/{productId}")
    fun delete(
        @PathVariable groupId: UUID,
        @PathVariable productId: UUID
    ) {
        saleProductService.delete(SecurityUtils.getCompanyId(), groupId, productId)
    }
}