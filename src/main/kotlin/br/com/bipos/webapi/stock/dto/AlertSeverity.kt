package br.com.bipos.webapi.stock.dto

enum class AlertSeverity {
    CRITICAL,    // Perda iminente (vencimento hoje/amanhã)
    HIGH,        // Perda próxima (3-7 dias)
    MEDIUM,      // Padrão de desperdício identificado
    LOW          // Sugestão de melhoria
}