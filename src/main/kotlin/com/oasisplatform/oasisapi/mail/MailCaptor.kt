package com.oasisplatform.oasisapi.mail

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Test-only component that records raw tokens sent by email (verification + password reset).
 * Only active under the "test" Spring profile; replaced by a null dependency in production.
 */
@Component
@Profile("test")
class MailCaptor {
    private val tokens: MutableMap<String, String> = ConcurrentHashMap()

    fun capture(email: String, token: String) {
        tokens[email.lowercase()] = token
    }

    fun getToken(email: String): String? = tokens[email.lowercase()]

    fun clear() = tokens.clear()
}

