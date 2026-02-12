package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.ProductExpiryBatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.*

interface ProductExpiryBatchRepository : JpaRepository<ProductExpiryBatch, UUID> {

    fun findByCompanyIdAndIsActiveTrue(companyId: UUID): List<ProductExpiryBatch>

    fun findByProductIdAndIsActiveTrue(productId: UUID): List<ProductExpiryBatch>

    @Query("SELECT p FROM ProductExpiryBatch p WHERE p.company.id = :companyId AND p.isActive = true AND p.expiryDate <= :date ORDER BY p.expiryDate ASC")
    fun findExpiringBatches(
        @Param("companyId") companyId: UUID,
        @Param("date") date: LocalDate
    ): List<ProductExpiryBatch>

    @Query("SELECT p FROM ProductExpiryBatch p WHERE p.company.id = :companyId AND p.isActive = true AND p.expiryDate <= CURRENT_DATE")
    fun findExpiredBatches(@Param("companyId") companyId: UUID): List<ProductExpiryBatch>
}
