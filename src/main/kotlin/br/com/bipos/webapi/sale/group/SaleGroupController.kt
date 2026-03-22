package br.com.bipos.webapi.sale.group

import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import br.com.bipos.webapi.sale.group.dto.SaleGroupCreateDTO
import br.com.bipos.webapi.sale.group.dto.SaleGroupDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/sale/groups", "/api/v1/sale/groups")
class SaleGroupController(
    private val saleGroupService: SaleGroupService
) {

    @GetMapping
    fun list(@CurrentUser user: AppUserDetails): List<SaleGroupDTO> =
        saleGroupService.list(user.requireCompanyId())

    @GetMapping("/{groupId}")
    fun getById(
        @PathVariable groupId: UUID,
        @CurrentUser user: AppUserDetails
    ): SaleGroupDTO =
        saleGroupService.getById(user.requireCompanyId(), groupId)

    @PostMapping
    fun create(
        @RequestBody dto: SaleGroupCreateDTO,
        @CurrentUser user: AppUserDetails
    ): SaleGroupDTO =
        saleGroupService.create(user.requireCompanyId(), dto)

    @PutMapping("/{groupId}")
    fun update(
        @PathVariable groupId: UUID,
        @RequestBody dto: SaleGroupCreateDTO,
        @CurrentUser user: AppUserDetails
    ): SaleGroupDTO =
        saleGroupService.update(user.requireCompanyId(), groupId, dto)

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable groupId: UUID,
        @CurrentUser user: AppUserDetails
    ) = saleGroupService.delete(user.requireCompanyId(), groupId)
}
