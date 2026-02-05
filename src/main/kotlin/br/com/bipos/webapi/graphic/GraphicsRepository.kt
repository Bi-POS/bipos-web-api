package br.com.bipos.webapi.graphic

import br.com.bipos.webapi.domain.catalog.Payment
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface GraphicsRepository : Repository<Payment, UUID> {

    /* =======================
       Vendas por hora (hoje)
    ======================= */
    @Query(
        """
        SELECT
            EXTRACT(HOUR FROM p.paidAt) as hour,
            SUM(p.amount)
        FROM Payment p
        JOIN p.sale s
        WHERE s.company.id = :companyId
          AND p.status = 'PAID'
          AND p.paidAt >= CURRENT_DATE
        GROUP BY hour
        ORDER BY hour
        """
    )
    fun salesByHour(
        @Param("companyId") companyId: UUID?
    ): List<Array<Any>>

    /* =======================
       Vendas por dia
    ======================= */
    @Query(
        """
        SELECT
            DATE(p.paidAt),
            SUM(p.amount)
        FROM Payment p
        JOIN p.sale s
        WHERE s.company.id = :companyId
          AND p.status = 'PAID'
          AND p.paidAt BETWEEN :start AND :end
        GROUP BY DATE(p.paidAt)
        ORDER BY DATE(p.paidAt)
        """
    )
    fun salesByDay(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    /* =======================
       Top produtos
    ======================= */
    @Query(
        """
        SELECT
            prod.name,
            SUM(i.quantity),
            SUM(i.subtotal)
        FROM Sale s
        JOIN s.items i
        JOIN i.product prod
        WHERE s.company.id = :companyId
          AND s.status = 'PAID'
        GROUP BY prod.name
        ORDER BY SUM(i.subtotal) DESC
        """
    )
    fun topProducts(
        @Param("companyId") companyId: UUID?
    ): List<Array<Any>>

    /* =======================
       Vendas por operador
    ======================= */
    @Query(
        """
        SELECT
            COALESCE(u.name, s.company.name),
            SUM(p.amount)
        FROM Payment p
        JOIN p.sale s
        LEFT JOIN p.user u
        WHERE s.company.id = :companyId
          AND p.status = 'PAID'
        GROUP BY COALESCE(u.name, s.company.name)
        ORDER BY SUM(p.amount) DESC
        """
    )
    fun salesByOperator(
        @Param("companyId") companyId: UUID?
    ): List<Array<Any>>
}
