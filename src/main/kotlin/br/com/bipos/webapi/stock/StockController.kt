package br.com.bipos.webapi.stock

import br.com.bipos.webapi.domain.stock.MovementType
import br.com.bipos.webapi.domain.stock.OperationType
import br.com.bipos.webapi.security.CurrentUser
import br.com.bipos.webapi.stock.dto.*
import br.com.bipos.webapi.user.AppUserDetails
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/v1/stock")
class StockController(
    private val stockService: StockService,
    private val stockRepository: StockRepository  // ✅ ADICIONADO!
) {

    // ============= OPERAÇÕES BÁSICAS DE ESTOQUE =============
    // ... (seus métodos existentes) ...

    // ============= AJUSTE DE MÍNIMO =============
    @PostMapping("/products/{productId}/set-minimum")
    fun setMinimumStock(
        @PathVariable productId: UUID,
        @RequestParam minimumQuantity: BigDecimal,
        @CurrentUser user: AppUserDetails
    ): ResponseEntity<StockResponse> {
        val stock = stockService.getStockByProductId(productId)

        stock.minimumQuantity = minimumQuantity
        stock.updatedAt = LocalDateTime.now()

        val updatedStock = stockRepository.save(stock)

        return ResponseEntity.ok(
            StockResponse(
                message = "✅ Quantidade mínima atualizada para $minimumQuantity unidades",
                productId = productId,
                productName = stock.product.name,
                currentQuantity = stock.currentQuantity,
                minimumQuantity = stock.minimumQuantity,
                availableQuantity = stock.availableQuantity,
                isLowStock = stock.isLowStock
            )
        )
    }

    private fun getMovementTypeDescription(type: MovementType): String {
        return when (type) {
            MovementType.PURCHASE -> "Compra"
            MovementType.SALE -> "Venda"
            MovementType.RESERVATION -> "Reserva"
            MovementType.RESERVATION_CANCEL -> "Cancelamento de reserva"
            MovementType.ADJUSTMENT_ADD -> "Ajuste +"
            MovementType.ADJUSTMENT_REMOVE -> "Ajuste -"
            MovementType.RETURN -> "Devolução"
            MovementType.LOSS -> "Perda/Desperdício"
            MovementType.TRANSFER_IN -> "Transferência recebida"
            MovementType.TRANSFER_OUT -> "Transferência enviada"
            MovementType.EVENT_CONSUMPTION -> "Consumo em operação"
            MovementType.EVENT_RETURN -> "Retorno de operação"
        }
    }
}