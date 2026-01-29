package br.com.bipos.webapi.init

import br.com.bipos.webapi.domain.module.Module
import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.module.ModuleRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ModuleInitializer(
    private val moduleRepository: ModuleRepository
) {

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        try {
            ModuleType.entries.forEach { type ->
                if (!moduleRepository.existsByName(type)) {
                    moduleRepository.save(Module(name = type))
                }
            }
        } catch (ex: Exception) {
            println("⚠️ ModuleInitializer ignorado: ${ex.message}")
        }
    }
}