package br.com.bipos.webapi.domain.user


import br.com.bipos.webapi.domain.company.Company
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
class AppUser(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.OPERATOR,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: Company? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    var photoUrl: String? = null,

    var updatePhotoAt: Instant = Instant.now(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
