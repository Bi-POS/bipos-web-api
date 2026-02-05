package br.com.bipos.webapi.graphic

import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/graphics")
class GraphicsController(
    private val service: GraphicsService
) {

    @GetMapping("/sales/by-hour")
    fun salesByHour(@AuthenticationPrincipal user: AppUserDetails) =
        service.salesByHour(user.user.company?.id)

    @GetMapping("/sales/by-day")
    fun salesByDay(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @AuthenticationPrincipal user: AppUserDetails
    ) =
        service.salesByDay(
            user.user.company?.id,
            startDate,
            endDate
        )

    @GetMapping("/products/top")
    fun topProducts(@AuthenticationPrincipal user: AppUserDetails) =
        service.topProducts(user.user.company?.id)

    @GetMapping("/sales/by-operator")
    fun salesByOperator(@AuthenticationPrincipal user: AppUserDetails) =
        service.salesByOperator(user.user.company?.id)
}
