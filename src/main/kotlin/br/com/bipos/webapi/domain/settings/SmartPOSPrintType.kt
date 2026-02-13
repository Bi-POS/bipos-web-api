// domain/smartpos/SmartPosPrint.kt
package br.com.bipos.webapi.domain.settings

enum class SmartPosPrint {
    NONE,       // Sem impressão
    SHORT,      // Comprovante resumido
    FULL;       // Comprovante completo

    companion object {
        fun fromString(value: String): SmartPosPrint {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                FULL // valor padrão
            }
        }
    }
}