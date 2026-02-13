// possettings/SmartPosSettingsRepository.kt
package br.com.bipos.webapi.possettings

import br.com.bipos.webapi.domain.settings.SmartPosSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SmartPosSettingsRepository : JpaRepository<SmartPosSettings, UUID> {

    fun findByCompanyIdAndIsActiveTrue(companyId: UUID): Optional<SmartPosSettings>

    fun findAllByCompanyIdAndIsActiveTrue(companyId: UUID): List<SmartPosSettings>

    @Modifying
    @Query("UPDATE SmartPosSettings s SET s.version = s.version + 1, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    fun incrementVersion(@Param("id") id: UUID)
}