package br.com.bipos.webapi.company

import br.com.bipos.webapi.company.dto.CompanyCreateDTO
import br.com.bipos.webapi.company.dto.CompanyDTO
import br.com.bipos.webapi.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/companies")
class CompanyController(
    private val companyService: CompanyService
) {

    @PostMapping
    fun create(@Valid @RequestBody dto: CompanyCreateDTO): ResponseEntity<CompanyDTO> {
        val result = companyService.create(dto)
        return ResponseEntity.status(201).body(result)
    }

    @GetMapping
    fun list(): ResponseEntity<List<CompanyDTO>> =
        ResponseEntity.ok(companyService.list())

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<CompanyDTO> =
        ResponseEntity.ok(companyService.getById(id))

    @GetMapping("/me")
    fun me(): ResponseEntity<CompanyDTO> {
        val companyId = SecurityUtils.getCompanyId()
        return ResponseEntity.ok(companyService.getById(companyId))
    }

    @PutMapping("/{id}/logo")
    fun updateLogo(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Void> {
        companyService.updateLogo(id, file)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        companyService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/logo")
    fun getLogo(@PathVariable id: UUID): ResponseEntity<Resource> {

        val path = Paths.get("/app/uploads/logos/company-$id.png")

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build()
        }

        val resource = UrlResource(path.toUri())

        return ResponseEntity.ok()
            .header("Cache-Control", "max-age=86400")
            .contentType(MediaType.IMAGE_PNG)
            .body(resource)
    }
}
