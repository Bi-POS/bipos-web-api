package br.com.bipos.webapi.user

import br.com.bipos.webapi.domain.user.AppUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class AppUserDetails(
    val user: AppUser
) : UserDetails {

    val id: UUID? = user.id

    override fun getUsername(): String = user.email

    override fun getPassword(): String = user.passwordHash

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = user.active

    fun getDomainUser(): AppUser = user
}
