package br.com.bipos.webapi.reports.sales

import br.com.bipos.webapi.payment.PaymentMethod
import br.com.bipos.webapi.reports.sales.dto.*
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/reports")
class SalesReportController(
    private val service: SalesReportService
) {

    @GetMapping("/sales")
    fun getSalesReport(
        @RequestParam(defaultValue = "day") groupBy: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesReportResponse> {

        val userDetails = authentication.principal as AppUserDetails
        val companyId = userDetails.user.company?.id

        return service.getSalesReport(
            companyId = companyId,
            groupBy = groupBy,
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/payment-methods")
    fun getSalesByPaymentMethod(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesByPaymentMethodResponse> {

        val user = authentication.principal as AppUserDetails
        val companyId = user.user.company?.id

        return service.getSalesByPaymentMethod(
            companyId = companyId,
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/payment-methods/{method}/details")
    fun getSalesByPaymentMethodDetail(
        @PathVariable method: PaymentMethod,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesByPaymentMethodDetailResponse> {

        val user = authentication.principal as AppUserDetails
        val companyId = user.user.company?.id

        return service.getSalesByPaymentMethodDetail(
            companyId = companyId,
            method = method,
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/payment-methods/{method}/products-by-day")
    fun reportByPaymentMethodProductsByDay(
        @PathVariable method: PaymentMethod,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @AuthenticationPrincipal user: AppUserDetails
    ): List<SalesByPaymentMethodProductDayResponse> {

        return service.reportByPaymentMethodProductsByDay(
            companyId = user.user.company?.id,
            method = method,
            start = startDate.atStartOfDay(),
            end = endDate.atTime(23, 59, 59)
        )
    }

    // 🔹 RESUMO POR POS
    @GetMapping("/sales/pos")
    fun getSalesByPos(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesByPosSummaryResponse> {

        val user = authentication.principal as AppUserDetails

        return service.getSalesByPos(
            companyId = user.user.company?.id,
            startDate = startDate,
            endDate = endDate
        )
    }

    // 🔹 POS × USUÁRIO (auditoria)
    @GetMapping("/sales/pos-by-user")
    fun getPosByUser(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<PosByUserResponse> {

        val user = authentication.principal as AppUserDetails

        return service.getPosByUser(
            companyId = user.user.company?.id,
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/pos/{serial}")
    fun getSalesByPosDetail(
        @PathVariable serial: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesByPosDetailResponse> {

        val user = authentication.principal as AppUserDetails

        return service.getSalesByPosDetail(
            companyId = user.user.company?.id,
            posSerial = serial,
            startDate = startDate,
            endDate = endDate
        )
    }

    // 🔹 VENDAS POR OPERADOR (ranking)
    @GetMapping("/sales/users")
    fun getSalesByUser(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        authentication: Authentication
    ): List<SalesByUserResponse> {

        val user = authentication.principal as AppUserDetails

        return service.getSalesByUser(
            companyId = user.user.company?.id,
            startDate = startDate,
            endDate = endDate
        )
    }

}
