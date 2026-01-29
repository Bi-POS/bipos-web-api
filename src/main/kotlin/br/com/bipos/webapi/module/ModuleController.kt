package br.com.bipos.webapi.module


import br.com.bipos.webapi.domain.module.ModuleType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import br.com.bipos.webapi.domain.module.Module

data class CreateModuleRequest(val name: String = "")

@RestController
@RequestMapping("/modules")
class ModuleController(private val moduleRepository: ModuleRepository) {

    @GetMapping
    fun list() = moduleRepository.findAll()

    @PostMapping
    fun create(): ResponseEntity<Module> {
        val module = Module(name = ModuleType.SALE)
        val saved = moduleRepository.save(module)
        return ResponseEntity.status(201).body(saved)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID) = moduleRepository.findById(id).map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())
}
