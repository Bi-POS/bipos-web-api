package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.sale.product.dto.SaleProductDTO
import br.com.bipos.webapi.security.SecurityUtils
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sale/products")
@CrossOrigin(origins = ["http://localhost:5173"])
class SaleProductGlobalController(
    private val saleProductService: SaleProductService
) {

    @GetMapping
    fun listAll(): List<SaleProductDTO> =
        saleProductService.listAll(SecurityUtils.getCompanyId())
}
