package br.com.bipos.webapi.graphic

import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/graphics", "/api/v1/graphics")
class GraphicsController(
    private val service: GraphicsService
) {

    @GetMapping("/sales/by-hour")
    fun salesByHour(@CurrentUser user: AppUserDetails) =
        service.salesByHour(user.requireCompanyId())

    @GetMapping("/sales/by-day")
    fun salesByDay(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ) =
        service.salesByDay(
            user.requireCompanyId(),
            startDate,
            endDate
        )

    @GetMapping("/products/top")
    fun topProducts(@CurrentUser user: AppUserDetails) =
        service.topProducts(user.requireCompanyId())

    @GetMapping("/sales/by-operator")
    fun salesByOperator(@CurrentUser user: AppUserDetails) =
        service.salesByOperator(user.requireCompanyId())
}
