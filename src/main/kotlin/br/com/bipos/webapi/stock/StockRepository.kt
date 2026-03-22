package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.Stock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface StockRepository : JpaRepository<Stock, UUID> {

    fun findByProductId(productId: UUID): Stock?

    fun findByCompanyId(companyId: UUID?): List<Stock>

    fun countByCompanyId(companyId: UUID): Long

    @Query("SELECT s FROM Stock s WHERE s.company.id = :companyId AND s.currentQuantity <= s.minimumQuantity")
    fun findLowStockByCompanyId(@Param("companyId") companyId: UUID?): List<Stock>

    @Query("SELECT s FROM Stock s WHERE s.company.id = :companyId ORDER BY s.currentQuantity ASC")
    fun findAllOrderByQuantityAsc(@Param("companyId") companyId: UUID): List<Stock>
}




