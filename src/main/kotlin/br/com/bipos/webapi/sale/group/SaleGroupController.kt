package br.com.bipos.webapi.sale.group

import br.com.bipos.webapi.sale.group.dto.SaleGroupCreateDTO
import br.com.bipos.webapi.sale.group.dto.SaleGroupDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/sale/groups")
class SaleGroupController(
    private val saleGroupService: SaleGroupService
) {

    @GetMapping
    fun list(): List<SaleGroupDTO> =
        saleGroupService.list()

    @GetMapping("/{groupId}")
    fun getById(
        @PathVariable groupId: UUID
    ): SaleGroupDTO =
        saleGroupService.getById(groupId)

    @PostMapping
    fun create(
        @RequestBody dto: SaleGroupCreateDTO
    ): SaleGroupDTO =
        saleGroupService.create(dto)

    @PutMapping("/{groupId}")
    fun update(
        @PathVariable groupId: UUID,
        @RequestBody dto: SaleGroupCreateDTO
    ): SaleGroupDTO =
        saleGroupService.update(groupId, dto)

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable groupId: UUID
    ) = saleGroupService.delete(groupId)
}
