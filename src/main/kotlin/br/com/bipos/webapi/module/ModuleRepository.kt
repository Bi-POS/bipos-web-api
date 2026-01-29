package br.com.bipos.webapi.module


import br.com.bipos.webapi.domain.module.ModuleType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import br.com.bipos.webapi.domain.module.Module

interface ModuleRepository : JpaRepository<Module, UUID> {

    fun findByName(name: ModuleType): Module?

    fun existsByName(name: ModuleType): Boolean
}
