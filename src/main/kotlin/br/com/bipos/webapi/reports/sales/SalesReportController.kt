package br.com.bipos.webapi.reports.sales

import br.com.bipos.webapi.payment.PaymentMethod
import br.com.bipos.webapi.reports.sales.dto.*
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.security.requireCompanyId
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/reports", "/api/v1/reports")
class SalesReportController(
    private val service: SalesReportService
) {

    @GetMapping("/sales")
    fun getSalesReport(
        @RequestParam(defaultValue = "day") groupBy: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ): List<SalesReportResponse> {
        return service.getSalesReport(
            companyId = user.requireCompanyId(),
            groupBy = groupBy,
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/payment-methods")
    fun getSalesByPaymentMethod(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ): List<SalesByPaymentMethodResponse> {
        return service.getSalesByPaymentMethod(
            companyId = user.requireCompanyId(),
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/payment-methods/{method}/details")
    fun getSalesByPaymentMethodDetail(
        @PathVariable method: PaymentMethod,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ): List<SalesByPaymentMethodDetailResponse> {
        return service.getSalesByPaymentMethodDetail(
            companyId = user.requireCompanyId(),
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
        @CurrentUser user: AppUserDetails
    ): List<SalesByPaymentMethodProductDayResponse> {

        return service.reportByPaymentMethodProductsByDay(
            companyId = user.requireCompanyId(),
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
        @CurrentUser user: AppUserDetails
    ): List<SalesByPosSummaryResponse> {
        return service.getSalesByPos(
            companyId = user.requireCompanyId(),
            startDate = startDate,
            endDate = endDate
        )
    }

    // 🔹 POS × USUÁRIO (auditoria)
    @GetMapping("/sales/pos-by-user")
    fun getPosByUser(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ): List<PosByUserResponse> {
        return service.getPosByUser(
            companyId = user.requireCompanyId(),
            startDate = startDate,
            endDate = endDate
        )
    }

    @GetMapping("/sales/pos/{serial}")
    fun getSalesByPosDetail(
        @PathVariable serial: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @CurrentUser user: AppUserDetails
    ): List<SalesByPosDetailResponse> {
        return service.getSalesByPosDetail(
            companyId = user.requireCompanyId(),
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
        @CurrentUser user: AppUserDetails
    ): List<SalesByUserResponse> {
        return service.getSalesByUser(
            companyId = user.requireCompanyId(),
            startDate = startDate,
            endDate = endDate
        )
    }

}
