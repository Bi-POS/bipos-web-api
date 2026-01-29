//package br.com.bipay.webapi.security.auth
//
//import br.com.bipay.login.QrService
//import br.com.bipay.webapi.company.CompanyRepository
//import br.com.bipay.webapi.exception.UserNotFoundException
//import br.com.bipay.webapi.login.request.QrRequest
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/security/auth")
//class AuthController(
//    private val companyRepository: CompanyRepository,
//    private val authService: AuthService,
////    private val qrService: QrService
//) {
//
//    @PostMapping("/qr")
//    fun createQr(@RequestBody req: QrRequest): ResponseEntity<Map<String, String>> {
//        val company = companyRepository.findByEmail(req.company)
//            ?: throw UserNotFoundException("Usuário '${req.company}' não encontrado")
//        val imageBase64 = qrService.createQrFor(company.name)
//        return ResponseEntity.ok(mapOf("qr" to imageBase64))
//    }
//
//}