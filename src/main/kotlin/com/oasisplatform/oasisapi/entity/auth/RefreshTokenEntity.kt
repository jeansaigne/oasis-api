package com.oasisplatform.oasisapi.entity.auth

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    var tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(nullable = false)
    var revoked: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

