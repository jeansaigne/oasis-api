package com.oasisplatform.oasisapi.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${app.auth.jwt.secret}") secret: String,
    @Value("\${app.auth.jwt.access-ttl-seconds}") private val accessTtlSeconds: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun generateAccessToken(userId: UUID, username: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    val accessTtlSecondsValue: Long get() = accessTtlSeconds
}

