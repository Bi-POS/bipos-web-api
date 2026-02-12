package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.MovementType
import br.com.bipos.webapi.domain.stock.StockMovement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface StockMovementRepository : JpaRepository<StockMovement, UUID> {

    fun findByProductIdAndMovementDateAfterOrderByMovementDateDesc(
        productId: UUID,
        date: LocalDateTime
    ): List<StockMovement>

    // Busca todas movimentações de um produto ordenadas por data
    fun findByProductIdOrderByMovementDateDesc(productId: UUID?): List<StockMovement>

    // Busca movimentações de uma empresa
    fun findByCompanyIdOrderByMovementDateDesc(companyId: UUID): List<StockMovement>

    // Busca movimentações por venda
    fun findBySaleId(saleId: UUID): List<StockMovement>

    // Busca movimentações por evento
    fun findByEventId(eventId: UUID): List<StockMovement>

    // Busca compras após uma data
    @Query("SELECT sm FROM StockMovement sm WHERE sm.company.id = :companyId AND sm.type = 'PURCHASE' AND sm.movementDate >= :startDate ORDER BY sm.movementDate DESC")
    fun findPurchasesByCompanyAndDateAfter(
        @Param("companyId") companyId: UUID,
        @Param("startDate") startDate: LocalDateTime
    ): List<StockMovement>

    // Busca perdas/desperdícios após uma data
    @Query("SELECT sm FROM StockMovement sm WHERE sm.company.id = :companyId AND sm.type = 'LOSS' AND sm.movementDate >= :startDate ORDER BY sm.movementDate DESC")
    fun findLossesByCompanyAndDateAfter(
        @Param("companyId") companyId: UUID,
        @Param("startDate") startDate: LocalDateTime
    ): List<StockMovement>

    // Busca vendas após uma data
    @Query("SELECT sm FROM StockMovement sm WHERE sm.company.id = :companyId AND sm.type = 'SALE' AND sm.movementDate >= :startDate ORDER BY sm.movementDate DESC")
    fun findSalesByCompanyAndDateAfter(
        @Param("companyId") companyId: UUID,
        @Param("startDate") startDate: LocalDateTime
    ): List<StockMovement>

    // Busca movimentações por período
    fun findByCompanyIdAndMovementDateBetweenOrderByMovementDateDesc(
        companyId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<StockMovement>

    // Busca movimentações por tipo e período
    fun findByCompanyIdAndTypeAndMovementDateBetweenOrderByMovementDateDesc(
        companyId: UUID,
        type: MovementType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<StockMovement>


    // Soma total de quantidade por tipo em um período
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.company.id = :companyId AND sm.type = :type AND sm.movementDate BETWEEN :startDate AND :endDate")
    fun sumQuantityByTypeAndPeriod(
        @Param("companyId") companyId: UUID,
        @Param("type") type: MovementType,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal

    // Busca últimas movimentações de uma empresa
    fun findTop10ByCompanyIdOrderByMovementDateDesc(companyId: UUID): List<StockMovement>

    // Busca movimentações por usuário
    fun findByUserIdOrderByMovementDateDesc(userId: UUID): List<StockMovement>
}