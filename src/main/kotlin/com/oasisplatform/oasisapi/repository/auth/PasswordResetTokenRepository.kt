package com.oasisplatform.oasisapi.repository.auth

import com.oasisplatform.oasisapi.entity.auth.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<PasswordResetTokenEntity>
}

