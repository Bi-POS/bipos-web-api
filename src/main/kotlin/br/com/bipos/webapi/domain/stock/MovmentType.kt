package br.com.bipos.webapi.domain.stock

enum class MovementType {
    PURCHASE,           // Compra/Entrada
    SALE,              // Venda/Saída
    RESERVATION,       // Reserva para venda
    RESERVATION_CANCEL, // Cancelamento de reserva
    ADJUSTMENT_ADD,    // Ajuste manual - adição
    ADJUSTMENT_REMOVE, // Ajuste manual - remoção
    RETURN,            // Devolução de cliente
    LOSS,              // Perda/Quebra/Vencimento
    TRANSFER_IN,       // Transferência recebida
    TRANSFER_OUT,      // Transferência enviada
    EVENT_CONSUMPTION, // Consumo em evento
    EVENT_RETURN       // Retorno de evento não consumido
}