package br.com.bipos.webapi.domain.companymodule

import br.com.bipos.webapi.domain.company.Company
import br.com.bipos.webapi.domain.module.Module
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "company_modules")
class CompanyModule(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var activatedAt: LocalDateTime? = null,

    @Column(nullable = true)
    var deactivatedAt: LocalDateTime? = null
) {

    // Método para ativar o módulo
    fun activate() {
        this.enabled = true
        this.activatedAt = LocalDateTime.now()
        this.deactivatedAt = null
        this.updatedAt = LocalDateTime.now()
    }

    // Método para desativar o módulo
    fun deactivate() {
        this.enabled = false
        this.deactivatedAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    // Método para verificar se está ativo
    fun isActive(): Boolean = this.enabled

    // Método para atualizar o módulo
    fun update(enabled: Boolean) {
        this.enabled = enabled
        this.updatedAt = LocalDateTime.now()

        if (enabled) {
            this.activatedAt = LocalDateTime.now()
            this.deactivatedAt = null
        } else {
            this.deactivatedAt = LocalDateTime.now()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as CompanyModule

        if (id != other.id) return false
        if (company.id != other.company.id) return false
        if (module.id != other.module.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (company.id?.hashCode() ?: 0)
        result = 31 * result + (module.id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CompanyModule(id=$id, companyId=${company.id}, moduleId=${module.id}, enabled=$enabled)"
    }
}