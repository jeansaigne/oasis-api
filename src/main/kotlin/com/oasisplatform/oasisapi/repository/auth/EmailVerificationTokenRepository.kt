package com.oasisplatform.oasisapi.repository.auth

import com.oasisplatform.oasisapi.entity.auth.EmailVerificationTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, UUID> {
    fun findByToken(token: String): Optional<EmailVerificationTokenEntity>
}

