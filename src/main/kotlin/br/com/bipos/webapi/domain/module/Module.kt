package br.com.bipos.webapi.domain.module

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(name = "modules")
class Module(

    @Id
    @UuidGenerator
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val name: ModuleType = ModuleType.SALE
)
