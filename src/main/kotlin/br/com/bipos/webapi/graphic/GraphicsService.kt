package br.com.bipos.webapi.graphic

import br.com.bipos.webapi.graphic.dto.SalesByDayResponse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Date
import java.time.LocalDate
import java.util.*

@Service
class GraphicsService(
    private val repository: GraphicsRepository
) {

    fun salesByHour(companyId: UUID?) =
        repository.salesByHour(companyId).map {
            mapOf(
                "hour" to (it[0] as Number).toInt(),
                "total" to it[1]
            )
        }

    fun salesByDay(
        companyId: UUID?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SalesByDayResponse> {

        val start = startDate.atStartOfDay()
        val end = endDate.atTime(23, 59, 59)

        return repository.salesByDay(
            companyId = companyId,
            start = start,
            end = end
        ).map {
            SalesByDayResponse(
                date = (it[0] as Date).toLocalDate(),
                total = it[1] as BigDecimal
            )
        }
    }

    fun topProducts(companyId: UUID?) =
        repository.topProducts(companyId).map {
            mapOf(
                "product" to it[0],
                "quantity" to it[1],
                "total" to it[2]
            )
        }

    fun salesByOperator(companyId: UUID?) =
        repository.salesByOperator(companyId).map {
            mapOf(
                "operator" to it[0],
                "total" to it[1]
            )
        }
}
