package br.com.bipos.webapi.init

import br.com.bipos.webapi.domain.module.ModuleType
import br.com.bipos.webapi.module.ModuleRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import br.com.bipos.webapi.domain.module.Module

@Component
class ModuleInitializer(
    private val moduleRepository: ModuleRepository
) {

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        ModuleType.entries.forEach { type ->
            if (!moduleRepository.existsByName(type)) {
                moduleRepository.save(Module(name = type))
            }
        }
    }
}
