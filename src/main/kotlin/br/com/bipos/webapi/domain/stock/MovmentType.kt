package br.com.bipos.webapi.domain.stock

enum class MovementType(
    val description: String,
    val signal: Int   // +1 entra | -1 sai
) {

    PURCHASE("Compra", +1),
    SALE("Venda", -1),
    RESERVATION("Reserva", -1),
    RESERVATION_CANCEL("Cancelamento de Reserva", +1),
    ADJUSTMENT_ADD("Ajuste +", +1),
    ADJUSTMENT_REMOVE("Ajuste -", -1),
    RETURN("Devolução", +1),
    LOSS("Perda", -1),
    TRANSFER_IN("Transferência Recebida", +1),
    TRANSFER_OUT("Transferência Enviada", -1),
    EVENT_CONSUMPTION("Consumo em Evento", -1),
    EVENT_RETURN("Retorno de Evento", +1);
}
