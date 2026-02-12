package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.ConsumptionRecord
import br.com.bipos.webapi.domain.stock.OperationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ConsumptionRecordRepository : JpaRepository<ConsumptionRecord, UUID> {

    fun findByOperationPointIdOrderByRecordDateDesc(operationPointId: UUID?): List<ConsumptionRecord>

    @Query("SELECT cr FROM ConsumptionRecord cr WHERE cr.operationPoint.company.id = :companyId ORDER BY cr.recordDate DESC")
    fun findByCompanyId(@Param("companyId") companyId: UUID): List<ConsumptionRecord>

    @Query(
        """
        SELECT cr FROM ConsumptionRecord cr 
        WHERE cr.operationPoint.operationType = :operationType 
        AND cr.operationPoint.company.id = :companyId 
        ORDER BY cr.recordDate DESC
    """
    )
    fun findByOperationType(
        @Param("companyId") companyId: UUID,
        @Param("operationType") operationType: OperationType
    ): List<ConsumptionRecord>

    @Query(
        """
        SELECT AVG(cr.wastePercentage) 
        FROM ConsumptionRecord cr 
        WHERE cr.operationPoint.company.id = :companyId 
        AND cr.operationPoint.operationType = :operationType
    """
    )
    fun averageWasteByOperationType(
        @Param("companyId") companyId: UUID,
        @Param("operationType") operationType: OperationType
    ): Double?

    @Query(
        """
        SELECT cr.product.id, 
               SUM(cr.totalWasted) as totalWasted,
               SUM(cr.totalWasted * cr.product.price) as totalWasteCost
        FROM ConsumptionRecord cr
        WHERE cr.operationPoint.company.id = :companyId
        GROUP BY cr.product.id
        ORDER BY totalWasteCost DESC
    """
    )
    fun findTopWastedProducts(@Param("companyId") companyId: UUID): List<Array<Any>>
}