package com.oasisplatform.oasisapi.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieService(
    @Value("\${app.auth.cookie.secure}") private val secure: Boolean,
    @Value("\${app.auth.cookie.same-site}") private val sameSite: String,
    @Value("\${app.auth.cookie.domain:}") private val domain: String,
    @Value("\${app.auth.jwt.access-ttl-seconds}") private val accessTtl: Long,
    @Value("\${app.auth.jwt.refresh-ttl-seconds}") private val refreshTtl: Long
) {
    companion object {
        const val ACCESS_COOKIE = "access_token"
        const val REFRESH_COOKIE = "refresh_token"
    }

    fun buildAccessCookie(token: String): ResponseCookie =
        base(ACCESS_COOKIE, token, Duration.ofSeconds(accessTtl)).build()

    fun buildRefreshCookie(token: String): ResponseCookie =
        base(REFRESH_COOKIE, token, Duration.ofSeconds(refreshTtl))
            .path("/api/auth")
            .build()

    fun clearAccessCookie(): ResponseCookie =
        base(ACCESS_COOKIE, "", Duration.ZERO).build()

    fun clearRefreshCookie(): ResponseCookie =
        base(REFRESH_COOKIE, "", Duration.ZERO).path("/api/auth").build()

    private fun base(name: String, value: String, maxAge: Duration): ResponseCookie.ResponseCookieBuilder {
        val builder = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(maxAge)
        if (domain.isNotBlank()) builder.domain(domain)
        return builder
    }
}

