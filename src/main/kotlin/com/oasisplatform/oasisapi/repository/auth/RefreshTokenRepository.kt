package com.oasisplatform.oasisapi.repository.auth

import com.oasisplatform.oasisapi.entity.auth.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshTokenEntity>

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
    fun revokeAllByUserId(@Param("userId") userId: UUID): Int
}

