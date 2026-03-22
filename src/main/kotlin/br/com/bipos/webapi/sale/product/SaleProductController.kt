package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.sale.product.dto.SaleProductCreateDTO
import br.com.bipos.webapi.sale.product.dto.SaleProductDTO
import br.com.bipos.webapi.sale.product.dto.SaleProductSummaryDTO
import br.com.bipos.webapi.sale.product.dto.toSummaryDTO
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/sale", "/api/v1/sale")
class SaleProductController(
    private val saleProductService: SaleProductService
) {

    @GetMapping("/products/all")
    fun getAllProductsByCompany(
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<List<SaleProductSummaryDTO>> {
        val products = saleProductService.listAll(user.requireCompanyId())
        return ResponseEntity.ok(products.map { it.toSummaryDTO() })
    }

    // ============= ENDPOINTS EXISTENTES =============
    @PostMapping("/groups/{groupId}/products")
    fun create(
        @PathVariable groupId: UUID,
        @Valid @RequestBody dto: SaleProductCreateDTO,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<SaleProductDTO> = ResponseEntity.status(HttpStatus.CREATED)
        .body(saleProductService.create(user.requireCompanyId(), groupId, dto))

    @GetMapping("/groups/{groupId}/products")
    fun list(
        @PathVariable groupId: UUID,
        @CurrentUser user: AppUserDetails
    ): List<SaleProductDTO> = saleProductService.list(user.requireCompanyId(), groupId)

    @PutMapping("/groups/{groupId}/products/{productId}")
    fun update(
        @PathVariable groupId: UUID,
        @PathVariable productId: UUID,
        @Valid @RequestBody dto: SaleProductCreateDTO,
        @CurrentUser user: AppUserDetails
    ): SaleProductDTO = saleProductService.update(user.requireCompanyId(), groupId, productId, dto)

    @DeleteMapping("/groups/{groupId}/products/{productId}")
    fun delete(
        @PathVariable groupId: UUID,
        @PathVariable productId: UUID,
        @CurrentUser user: AppUserDetails
    ) {
        saleProductService.delete(user.requireCompanyId(), groupId, productId)
    }
}
