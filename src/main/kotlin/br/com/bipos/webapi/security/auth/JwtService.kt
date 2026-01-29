package br.com.bipos.webapi.security.auth

import br.com.bipos.webapi.domain.user.AppUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService(
    private val props: WebJwtProperties
) {

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(props.secret)
        )
    }

    private val validityMs: Long
        get() = props.expiration

    // ===============================
    // TOKEN GENERATION
    // ===============================

    fun generateToken(user: AppUser): String {
        val now = Date()
        val exp = Date(now.time + validityMs)

        val claims = Jwts.claims().setSubject(user.email)
        claims["userId"] = user.id.toString()
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
