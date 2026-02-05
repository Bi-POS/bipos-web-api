package br.com.bipos.webapi.reports.sales

import br.com.bipos.webapi.payment.PaymentMethod
import br.com.bipos.webapi.reports.sales.dto.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
class SalesReportService(
    private val repository: SalesReportRepository
) {

    /* =======================
       VENDAS POR PERÍODO
    ======================= */
    fun getSalesReport(
        companyId: UUID?,
        groupBy: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesReportResponse> {

        val start = startDate.atStartOfDay()
        val end = endDate.atTime(LocalTime.MAX)

        val rows = when (groupBy) {
            "month" -> repository.reportByMonth(companyId, start, end)
            else -> repository.reportByDay(companyId, start, end)
        }

        return rows.map {
            SalesReportResponse(
                period = it[0].toString(),
                total = it[1] as BigDecimal,
                count = (it[2] as Number).toLong()
            )
        }
    }

    /* =======================
       POR FORMA DE PAGAMENTO
    ======================= */
    fun getSalesByPaymentMethod(
        companyId: UUID?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByPaymentMethodResponse> {

        return repository.reportByPaymentMethod(
            companyId,
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX)
        ).map {
            SalesByPaymentMethodResponse(
                method = it[0].toString(),
                total = it[1] as BigDecimal,
                count = (it[2] as Number).toLong()
            )
        }
    }

    fun getSalesByPaymentMethodDetail(
        companyId: UUID?,
        method: PaymentMethod,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByPaymentMethodDetailResponse> {

        return repository.reportSalesByPaymentMethodDetail(
            companyId,
            method,
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX)
        ).map {
            SalesByPaymentMethodDetailResponse(
                saleId = it[0] as UUID,
                date = (it[1] as Date).toLocalDate(),
                receivedAmount = it[2] as BigDecimal,
                itemsCount = (it[3] as Number).toInt(),
                products = it[4].toString().split(", ")
            )
        }
    }

    fun reportByPaymentMethodProductsByDay(
        companyId: UUID?,
        method: PaymentMethod,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<SalesByPaymentMethodProductDayResponse> {

        return repository.reportSalesByPaymentMethodByProductAndDay(
            companyId,
            method,
            start,
            end
        ).map {
            SalesByPaymentMethodProductDayResponse(
                date = (it[0] as Date).toLocalDate(),
                productId = it[1] as UUID,
                productName = it[2] as String,
                quantity = (it[3] as Number).toInt(),
                totalReceived = it[4] as BigDecimal
            )
        }
    }

    /* =======================
    POS — RESUMO
 ======================= */
    fun getSalesByPos(
        companyId: UUID?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByPosSummaryResponse> {

        return repository.reportSalesByPos(
            companyId,
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        ).map {
            SalesByPosSummaryResponse(
                posSerial = it[0] as String,
                totalReceived = it[1] as BigDecimal,
                salesCount = (it[2] as Number).toLong()
            )
        }
    }

    /* =======================
       POS — DETALHE
    ======================= */
    fun getSalesByPosDetail(
        companyId: UUID?,
        posSerial: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByPosDetailResponse> {

        return repository.reportSalesDetailByPos(
            companyId,
            posSerial,
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        ).map {
            SalesByPosDetailResponse(
                saleId = it[0] as UUID,
                date = (it[1] as Date).toLocalDate(),
                userId = it[2] as UUID,
                userName = it[3] as String,
                method = it[4].toString(),
                totalReceived = it[5] as BigDecimal,
                products = it[6].toString().split(", ")
            )
        }
    }

    /* =======================
       POS × USUÁRIO
    ======================= */
    fun getPosByUser(
        companyId: UUID?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PosByUserResponse> {

        return repository.reportPosByUser(
            companyId,
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        ).map {
            PosByUserResponse(
                posSerial = it[0] as String,
                operatorName = it[1] as String,
                totalReceived = it[2] as BigDecimal,
                paymentsCount = (it[3] as Number).toLong()
            )
        }
    }

    fun getSalesByUser(
        companyId: UUID?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByUserResponse> {

        val start = startDate.atStartOfDay()
        val end = endDate.atTime(23, 59, 59)

        return repository
            .reportSalesByUser(companyId, start, end)
            .map {
                SalesByUserResponse(
                    userId = it[0] as UUID,
                    operatorName = it[1] as String,
                    totalReceived = it[2] as BigDecimal,
                    salesCount = (it[3] as Number).toLong()
                )
            }
    }
}
