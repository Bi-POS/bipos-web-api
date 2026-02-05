package br.com.bipos.webapi.security.auth

import br.com.bipos.webapi.domain.user.AppUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService(

    @Value("\${security.jwt.web.secret}")
    private val secret: String,

    @Value("\${security.jwt.web.expiration}")
    private val expiration: Long
) {

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(
            secret.toByteArray(Charsets.UTF_8)
        )
    }

    init {
        println("JWT SECRET carregado? ${secret.isNotBlank()}")
    }


    // ===============================
    // TOKEN GENERATION
    // ===============================

    fun generateToken(user: AppUser): String {
        val now = Date()
        val exp = Date(now.time + expiration)

        val claims = Jwts.claims().setSubject(user.email)
        claims["userId"] = user.id.toString()
        claims["userName"] = user.name
        claims["companyId"] = user.company?.id.toString()
        claims["role"] = user.role.name
        claims["modules"] = user.company?.modules?.map { it.module?.name }

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // ===============================
    // TOKEN VALIDATION
    // ===============================

    fun extractUsername(token: String): String? =
        extractClaim(token) { it.subject }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    // ===============================
    // CLAIMS
    // ===============================

    fun extractCompanyId(token: String): String =
        extractAllClaims(token)["companyId"] as String

    fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    // ===============================
    // PRIVATE
    // ===============================

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token) { it.expiration }
        return expiration.before(Date())
    }

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return resolver(claims)
    }
}
