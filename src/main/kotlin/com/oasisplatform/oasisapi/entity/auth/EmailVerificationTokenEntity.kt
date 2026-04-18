package com.oasisplatform.oasisapi.entity.auth

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "email_verification_tokens")
class EmailVerificationTokenEntity(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Column(nullable = false, unique = true, length = 255)
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(nullable = false)
    var used: Boolean = false
)

