package com.oasisplatform.oasisapi.repository.auth

import com.oasisplatform.oasisapi.entity.auth.EmailVerificationTokenEntity
import com.oasisplatform.oasisapi.entity.auth.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, UUID> {
    fun findByToken(token: String): Optional<EmailVerificationTokenEntity>

    @Modifying
    @Query("update EmailVerificationTokenEntity t set t.used = true where t.user = :user and t.used = false")
    fun markAllAsUsedForUser(@Param("user") user: UserEntity): Int
}

