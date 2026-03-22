package br.com.bipos.webapi.module


import br.com.bipos.webapi.domain.module.Module
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.exception.ResourceNotFoundException
import br.com.bipos.webapi.module.dto.ModuleResponseDTO
import br.com.bipos.webapi.module.dto.toResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

data class CreateModuleRequest(val name: String = "")

@RestController
@RequestMapping("/modules", "/api/v1/modules")
class ModuleController(private val moduleRepository: ModuleRepository) {

    @GetMapping
    fun list(): List<ModuleResponseDTO> = moduleRepository.findAll().map { it.toResponseDTO() }

    @PostMapping
    fun create(): ResponseEntity<ModuleResponseDTO> {
        val module = Module(name = ModuleType.SALE)
        val saved = moduleRepository.save(module)
        return ResponseEntity.status(201).body(saved.toResponseDTO())
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<ModuleResponseDTO> {
        val module = moduleRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Módulo não encontrado") }
        return ResponseEntity.ok(module.toResponseDTO())
    }
}
