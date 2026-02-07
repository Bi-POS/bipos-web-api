package br.com.bipos.webapi.domain.login

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "smartpos_qr_tokens")
class SmartPosLoginToken(

    @Id
    @Column(length = 36, nullable = false)
    val token: String,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val companyId: UUID,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    var used: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

