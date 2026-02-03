package br.com.bipos.webapi.domain.login

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "smartpos_login_tokens",
    indexes = [
        Index(name = "idx_smartpos_token_expires", columnList = "expiresAt"),
        Index(name = "idx_smartpos_token_used", columnList = "used")
    ]
)
class SmartPosLoginToken(

    @Id
    @Column(length = 36, nullable = false)
    val token: String,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val companyId: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    var used: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
