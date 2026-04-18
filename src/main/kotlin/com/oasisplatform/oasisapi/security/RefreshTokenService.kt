package com.oasisplatform.oasisapi.security

import com.oasisplatform.oasisapi.entity.auth.RefreshTokenEntity
import com.oasisplatform.oasisapi.entity.auth.UserEntity
import com.oasisplatform.oasisapi.repository.auth.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.HexFormat

@Component
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${app.auth.jwt.refresh-ttl-seconds}") private val refreshTtlSeconds: Long
) {
    private val random = SecureRandom()

    /** Returns the raw token (to put in cookie) and stores its hash. */
    fun issue(user: UserEntity): String {
        val raw = generateRaw()
        val entity = RefreshTokenEntity(
            user = user,
            tokenHash = hash(raw),
            expiresAt = LocalDateTime.now().plusSeconds(refreshTtlSeconds)
        )
        refreshTokenRepository.save(entity)
        return raw
    }

    fun findValid(rawToken: String): RefreshTokenEntity? {
        val entity = refreshTokenRepository.findByTokenHash(hash(rawToken)).orElse(null) ?: return null
        if (entity.revoked || entity.expiresAt.isBefore(LocalDateTime.now())) return null
        return entity
    }

    fun revoke(entity: RefreshTokenEntity) {
        entity.revoked = true
        refreshTokenRepository.save(entity)
    }

    fun revokeIfPresent(rawToken: String?) {
        if (rawToken.isNullOrBlank()) return
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent {
            it.revoked = true
            refreshTokenRepository.save(it)
        }
    }

    /** Revoke all active refresh tokens for a user. */
    fun revokeAllForUser(user: UserEntity) {
        user.id?.let { refreshTokenRepository.revokeAllByUserId(it) }
    }

    private fun generateRaw(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return HexFormat.of().formatHex(bytes)
    }

    fun hash(raw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return HexFormat.of().formatHex(md.digest(raw.toByteArray(Charsets.UTF_8)))
    }
}

