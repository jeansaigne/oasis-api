package com.oasisplatform.oasisapi.entity.auth

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 50)
    var username: String,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(name = "password_hash", length = 255)
    var passwordHash: String? = null,

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

