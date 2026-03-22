package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.OperationPoint
import br.com.bipos.webapi.domain.stock.OperationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface OperationPointRepository : JpaRepository<OperationPoint, UUID> {

    fun findByCompanyIdOrderByStartDateDesc(companyId: UUID): List<OperationPoint>

    fun findByIdAndCompanyId(id: UUID, companyId: UUID): OperationPoint?

    fun findByCompanyIdAndIsActiveTrue(companyId: UUID): List<OperationPoint>

    @Query("SELECT op FROM OperationPoint op WHERE op.company.id = :companyId AND op.startDate BETWEEN :startDate AND :endDate ORDER BY op.startDate DESC")
    fun findByCompanyIdAndDateRange(
        @Param("companyId") companyId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<OperationPoint>

    @Query("SELECT op FROM OperationPoint op WHERE op.company.id = :companyId AND op.operationType = :operationType ORDER BY op.startDate DESC")
    fun findByCompanyIdAndOperationType(
        @Param("companyId") companyId: UUID,
        @Param("operationType") operationType: OperationType
    ): List<OperationPoint>

    // ✅ CORRIGIDO: Trata startDate null
    @Query("SELECT op FROM OperationPoint op WHERE op.company.id = :companyId AND (op.startDate >= CURRENT_DATE OR op.startDate IS NULL) ORDER BY op.startDate ASC")
    fun findUpcomingOperations(@Param("companyId") companyId: UUID): List<OperationPoint>

    @Query("SELECT COUNT(op) FROM OperationPoint op WHERE op.company.id = :companyId")
    fun countByCompanyId(@Param("companyId") companyId: UUID): Long
}
