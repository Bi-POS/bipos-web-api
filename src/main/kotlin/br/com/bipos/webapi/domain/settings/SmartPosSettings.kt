// domain/settings/SmartPosSettings.kt
package br.com.bipos.webapi.domain.settings

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "smartpos_settings")
class SmartPosSettings(

    @Id
    @UuidGenerator
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val companyId: UUID,

    // ===== IMPRESSÃO =====
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var print: SmartPosPrint = SmartPosPrint.FULL,

    @Column(nullable = false)
    var printLogo: Boolean = false,

    @Column(nullable = true, length = 500)
    var logoUrl: String? = null,

    // ===== SEGURANÇA =====
    @Column(nullable = false)
    var securityEnabled: Boolean = false,

    @Column(nullable = true)
    var pinHash: String? = null,

    @Column(nullable = false)
    var pinAttempts: Int = 0,

    @Column(nullable = true)
    var lastPinChange: LocalDateTime? = null,

    // ===== COMPORTAMENTO =====
    @Column(nullable = false)
    var autoLogoutMinutes: Int = 5,

    @Column(nullable = false)
    var darkMode: Boolean = false,

    @Column(nullable = false)
    var soundEnabled: Boolean = true,

    // ===== CONTROLE =====
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var version: Long = 1,

    @Column(nullable = false)
    var isActive: Boolean = true
) {

    // 🔥 MÉTODO ADICIONADO: Atualizar PIN
    fun updatePin(newPinHash: String) {
        this.pinHash = newPinHash
        this.lastPinChange = LocalDateTime.now()
        this.pinAttempts = 0
        this.updatedAt = LocalDateTime.now()
    }

    // 🔥 MÉTODO ADICIONADO: Registrar tentativa de PIN
    fun registerPinAttempt(success: Boolean) {
        if (success) {
            this.pinAttempts = 0
        } else {
            this.pinAttempts++
        }
        this.updatedAt = LocalDateTime.now()
    }

    // 🔥 MÉTODO ADICIONADO: Verificar se PIN está bloqueado
    fun isPinBlocked(): Boolean {
        return pinAttempts >= 5
    }

    // 🔥 MÉTODO ADICIONADO: Resetar tentativas
    fun resetPinAttempts() {
        this.pinAttempts = 0
        this.updatedAt = LocalDateTime.now()
    }
}