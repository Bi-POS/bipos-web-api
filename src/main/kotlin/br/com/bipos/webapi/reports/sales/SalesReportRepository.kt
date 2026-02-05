package br.com.bipos.webapi.reports.sales

import br.com.bipos.webapi.domain.catalog.Sale
import br.com.bipos.webapi.payment.PaymentMethod
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface SalesReportRepository : Repository<Sale, UUID> {

    @Query(
        """
        SELECT
            DATE(s.createdAt) as period,
            SUM(s.totalAmount) as total,
            COUNT(s.id) as count
        FROM Sale s
        WHERE s.company.id = :companyId
          AND s.status = 'PAID'
          AND s.createdAt BETWEEN :start AND :end
        GROUP BY DATE(s.createdAt)
        ORDER BY period
        """
    )
    fun reportByDay(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
        SELECT
            TO_CHAR(s.createdAt, 'YYYY-MM') as period,
            SUM(s.totalAmount) as total,
            COUNT(s.id) as count
        FROM Sale s
        WHERE s.company.id = :companyId
          AND s.status = 'PAID'
          AND s.createdAt BETWEEN :start AND :end
        GROUP BY TO_CHAR(s.createdAt, 'YYYY-MM')
        ORDER BY period
        """
    )
    fun reportByMonth(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        p.method,
        SUM(p.amount),
        COUNT(DISTINCT s.id)
    FROM Sale s
    JOIN s.payments p
    WHERE s.company.id = :companyId
      AND s.status = 'PAID'
      AND p.status = 'PAID'
      AND s.createdAt BETWEEN :start AND :end
    GROUP BY p.method
    ORDER BY p.method
    """
    )
    fun reportByPaymentMethod(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        s.id,
        DATE(s.createdAt),
        SUM(p.amount),
        SUM(i.quantity),
        string_agg(prod.name, ', ')
    FROM Sale s
    JOIN s.payments p
    JOIN s.items i
    JOIN i.product prod
    WHERE s.company.id = :companyId
      AND s.status = 'PAID'
      AND p.status = 'PAID'
      AND p.method = :method
      AND s.createdAt BETWEEN :start AND :end
    GROUP BY s.id, DATE(s.createdAt)
    ORDER BY DATE(s.createdAt) DESC
    """
    )
    fun reportSalesByPaymentMethodDetail(
        @Param("companyId") companyId: UUID?,
        @Param("method") method: PaymentMethod,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        DATE(s.createdAt),
        prod.id,
        prod.name,
        SUM(i.quantity),
        SUM(i.subtotal)
    FROM Sale s
    JOIN s.payments p
    JOIN s.items i
    JOIN i.product prod
    WHERE s.company.id = :companyId
      AND s.status = 'PAID'
      AND p.status = 'PAID'
      AND p.method = :method
      AND s.createdAt BETWEEN :start AND :end
    GROUP BY DATE(s.createdAt), prod.id, prod.name
    ORDER BY DATE(s.createdAt) DESC, SUM(i.subtotal) DESC
    """
    )
    fun reportSalesByPaymentMethodByProductAndDay(
        @Param("companyId") companyId: UUID?,
        @Param("method") method: PaymentMethod,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        s.id,
        DATE(p.paidAt),
        u.id,
        u.name,
        p.method,
        SUM(p.amount),
        string_agg(prod.name, ', ')
    FROM Payment p
    JOIN p.sale s
    JOIN p.user u
    JOIN s.items i
    JOIN i.product prod
    WHERE s.company.id = :companyId
      AND p.posSerial = :posSerial
      AND p.status = 'PAID'
      AND p.paidAt BETWEEN :start AND :end
    GROUP BY s.id, DATE(p.paidAt), u.id, u.name, p.method
    ORDER BY DATE(p.paidAt) DESC
    """
    )
    fun reportSalesDetailByPos(
        @Param("companyId") companyId: UUID?,
        @Param("posSerial") posSerial: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        u.id,
        u.name,
        SUM(p.amount),
        COUNT(DISTINCT s.id)
    FROM Payment p
    JOIN p.user u
    JOIN p.sale s
    WHERE s.company.id = :companyId
      AND p.status = 'PAID'
      AND p.paidAt BETWEEN :start AND :end
    GROUP BY u.id, u.name
    ORDER BY SUM(p.amount) DESC
    """
    )
    fun reportSalesByUser(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        p.posSerial,
        COALESCE(u.name, c.name),
        SUM(p.amount),
        COUNT(p.id)
    FROM Payment p
    LEFT JOIN p.user u
    JOIN p.sale s
    JOIN s.company c
    WHERE s.company.id = :companyId
      AND p.status = 'PAID'
      AND p.paidAt BETWEEN :start AND :end
    GROUP BY p.posSerial, COALESCE(u.name, c.name)
    ORDER BY p.posSerial
    """
    )
    fun reportPosByUser(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>

    @Query(
        """
    SELECT
        p.posSerial,
        SUM(p.amount),
        COUNT(DISTINCT s.id)
    FROM Payment p
    JOIN p.sale s
    WHERE s.company.id = :companyId
      AND p.status = 'PAID'
      AND s.status = 'PAID'
      AND p.paidAt BETWEEN :start AND :end
    GROUP BY p.posSerial
    ORDER BY SUM(p.amount) DESC
    """
    )
    fun reportSalesByPos(
        @Param("companyId") companyId: UUID?,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Array<Any>>


}
