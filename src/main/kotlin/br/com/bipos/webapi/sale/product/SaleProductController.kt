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
@RequestMapping("/sale/groups/{groupId}/products")
class SaleProductController(
    private val saleProductService: SaleProductService
) {

    @PostMapping
    fun create(
        @PathVariable groupId: UUID, @Valid @RequestBody dto: SaleProductCreateDTO
    ): ResponseEntity<SaleProductDTO> = ResponseEntity.status(HttpStatus.CREATED)
        .body(saleProductService.create(SecurityUtils.getCompanyId(), groupId, dto))

    @GetMapping
    fun list(
        @PathVariable groupId: UUID
    ): List<SaleProductDTO> = saleProductService.list(SecurityUtils.getCompanyId(), groupId)

    @PutMapping("/{productId}")
    fun update(
        @PathVariable groupId: UUID, @PathVariable productId: UUID, @Valid @RequestBody dto: SaleProductCreateDTO
    ): SaleProductDTO = saleProductService.update(SecurityUtils.getCompanyId(), groupId, productId, dto)

    @DeleteMapping("/{productId}")
    fun delete(
        @PathVariable groupId: UUID, @PathVariable productId: UUID
    ) {
        saleProductService.delete(SecurityUtils.getCompanyId(), groupId, productId)
    }
}