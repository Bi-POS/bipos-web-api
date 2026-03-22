package br.com.bipos.webapi.sale.product

import br.com.bipos.webapi.sale.product.dto.SaleProductDTO
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sale/products", "/api/v1/sale/products")
class SaleProductGlobalController(
    private val saleProductService: SaleProductService
) {

    @GetMapping
    fun listAll(@CurrentUser user: AppUserDetails): List<SaleProductDTO> =
        saleProductService.listAll(user.requireCompanyId())
}
