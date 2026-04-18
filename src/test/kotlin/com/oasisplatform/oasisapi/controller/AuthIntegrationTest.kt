package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.support.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@Suppress("UNCHECKED_CAST")
class AuthIntegrationTest : BaseIntegrationTest() {

    private val validPassword = "Str0ng!Passw0rd"
    private val newValidPassword = "N3w!Str0ngPass"

    // ---------- Helpers --------------------------------------------------------------

    private fun jsonHeaders(cookies: List<String> = emptyList()): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            if (cookies.isNotEmpty()) set(HttpHeaders.COOKIE, cookies.joinToString("; "))
        }

    private fun post(
        path: String,
        body: Any? = null,
        cookies: List<String> = emptyList()
    ): ResponseEntity<Map<*, *>> =
        restTemplate.exchange(
            path,
            HttpMethod.POST,
            HttpEntity(body, jsonHeaders(cookies)),
            Map::class.java
        ) as ResponseEntity<Map<*, *>>

    private fun postRaw(
        path: String,
        body: Any? = null,
        cookies: List<String> = emptyList()
    ): ResponseEntity<String> =
        restTemplate.exchange(
            path,
            HttpMethod.POST,
            HttpEntity(body, jsonHeaders(cookies)),
            String::class.java
        )

    private fun get(
        path: String,
        cookies: List<String> = emptyList()
    ): ResponseEntity<Map<*, *>> =
        restTemplate.exchange(
            path,
            HttpMethod.GET,
            HttpEntity<Any>(jsonHeaders(cookies)),
            Map::class.java
        ) as ResponseEntity<Map<*, *>>

    private fun getRaw(
        path: String,
        cookies: List<String> = emptyList()
    ): ResponseEntity<String> =
        restTemplate.exchange(
            path,
            HttpMethod.GET,
            HttpEntity<Any>(jsonHeaders(cookies)),
            String::class.java
        )

    private fun register(
        email: String,
        username: String,
        password: String = validPassword
    ): ResponseEntity<Map<*, *>> =
        post("/api/auth/register", mapOf("username" to username, "email" to email, "password" to password))

    private fun verify(token: String): ResponseEntity<String> =
        getRaw("/api/auth/verify-email?token=$token")

    private fun login(identifier: String, password: String): ResponseEntity<Map<*, *>> =
        post("/api/auth/login", mapOf("identifier" to identifier, "password" to password))

    private fun registerAndVerify(
        email: String,
        username: String,
        password: String = validPassword
    ) {
        val res = register(email, username, password)
        assertThat(res.statusCode).isEqualTo(HttpStatus.CREATED)
        val token = mailCaptor.getToken(email)
            ?: error("No verification token captured for $email")
        assertThat(verify(token).statusCode).isEqualTo(HttpStatus.FOUND)
    }

    /** Parse a Set-Cookie header to extract the `name=value` part. */
    private fun parseCookie(setCookie: String): Pair<String, String> {
        val firstSegment = setCookie.substringBefore(';')
        val (name, value) = firstSegment.split('=', limit = 2)
        return name to value
    }

    private fun cookiesOf(response: ResponseEntity<*>): Map<String, String> =
        (response.headers[HttpHeaders.SET_COOKIE] ?: emptyList())
            .associate { parseCookie(it) }

    private fun cookieHeaderOf(response: ResponseEntity<*>, vararg names: String): List<String> {
        val jar = cookiesOf(response)
        return names.mapNotNull { name -> jar[name]?.takeIf { it.isNotEmpty() }?.let { "$name=$it" } }
    }

    // ---------- Register / Verify ----------------------------------------------------

    @Nested
    @DisplayName("POST /api/auth/register & GET /api/auth/verify-email")
    inner class RegisterAndVerify {

        @Test
        @DisplayName("registers a new user and captures the verification token by mail")
        fun registers_and_captures_token() {
            val res = register("alice@example.com", "alice")

            assertThat(res.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(res.body!!["username"]).isEqualTo("alice")
            assertThat(res.body!!["email"]).isEqualTo("alice@example.com")
            assertThat(res.body!!["emailVerified"]).isEqualTo(false)
            assertThat(res.body!!["id"]).isNotNull

            assertThat(mailCaptor.getToken("alice@example.com")).isNotNull
            val user = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow()
            assertThat(user.emailVerified).isFalse
        }

        @Test
        @DisplayName("returns 409 when the email is already registered")
        fun conflict_on_duplicate_email() {
            assertThat(register("dup@example.com", "first").statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(register("dup@example.com", "second").statusCode).isEqualTo(HttpStatus.CONFLICT)
        }

        @Test
        @DisplayName("returns 409 when the username is already registered")
        fun conflict_on_duplicate_username() {
            assertThat(register("one@example.com", "taken").statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(register("two@example.com", "taken").statusCode).isEqualTo(HttpStatus.CONFLICT)
        }

        @Test
        @DisplayName("returns 400 when password does not meet complexity rules")
        fun weak_password_is_rejected() {
            assertThat(register("weak@example.com", "weaker", "weakpass").statusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("verifies a valid token, redirects to /login?verified=true and flips emailVerified")
        fun verify_valid_token() {
            assertThat(register("bob@example.com", "bob").statusCode).isEqualTo(HttpStatus.CREATED)
            val token = mailCaptor.getToken("bob@example.com")!!

            val res = verify(token)
            assertThat(res.statusCode).isEqualTo(HttpStatus.FOUND)
            assertThat(res.headers.location!!.toString()).contains("/login?verified=true")

            val user = userRepository.findByEmailIgnoreCase("bob@example.com").orElseThrow()
            assertThat(user.emailVerified).isTrue
        }

        @Test
        @DisplayName("redirects to /login?verified=false on an invalid verification token")
        fun verify_invalid_token() {
            val res = verify("not-a-real-token")
            assertThat(res.statusCode).isEqualTo(HttpStatus.FOUND)
            assertThat(res.headers.location!!.toString()).contains("/login?verified=false")
        }
    }

    // ---------- Login ----------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/auth/login")
    inner class Login {

        @Test
        @DisplayName("returns 403 EMAIL_NOT_VERIFIED when the email is not verified")
        fun unverified_email_is_forbidden() {
            assertThat(register("eve@example.com", "eve").statusCode).isEqualTo(HttpStatus.CREATED)

            val res = login("eve@example.com", validPassword)
            assertThat(res.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            val details = res.body!!["details"] as List<*>
            assertThat(details).contains("EMAIL_NOT_VERIFIED")
        }

        @Test
        @DisplayName("returns 401 on wrong password")
        fun wrong_password() {
            registerAndVerify("carol@example.com", "carol")
            assertThat(login("carol@example.com", "WrongP@ss123").statusCode)
                .isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        @DisplayName("returns 401 on unknown identifier")
        fun unknown_user() {
            assertThat(login("ghost@example.com", validPassword).statusCode)
                .isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        @DisplayName("returns 200 and sets both cookies when logging in by email")
        fun login_by_email() {
            registerAndVerify("dave@example.com", "dave")
            val res = login("dave@example.com", validPassword)

            assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(res.body!!["username"]).isEqualTo("dave")
            val cookies = cookiesOf(res)
            assertThat(cookies["access_token"]).isNotBlank
            assertThat(cookies["refresh_token"]).isNotBlank
        }

        @Test
        @DisplayName("returns 200 and sets both cookies when logging in by username")
        fun login_by_username() {
            registerAndVerify("frank@example.com", "frank")
            val res = login("frank", validPassword)

            assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(res.body!!["username"]).isEqualTo("frank")
            val cookies = cookiesOf(res)
            assertThat(cookies["access_token"]).isNotBlank
            assertThat(cookies["refresh_token"]).isNotBlank
        }
    }

    // ---------- Session / me / refresh / logout --------------------------------------

    @Nested
    @DisplayName("Session management (me, refresh, logout)")
    inner class Session {

        @Test
        @DisplayName("GET /api/auth/me without cookies returns 401")
        fun me_without_cookie() {
            val res = getRaw("/api/auth/me")
            assertThat(res.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }

        @Test
        @DisplayName("GET /api/auth/me with a valid access_token returns the user profile")
        fun me_with_valid_cookie() {
            registerAndVerify("grace@example.com", "grace")
            val loginRes = login("grace@example.com", validPassword)
            val cookies = cookieHeaderOf(loginRes, "access_token")

            val res = get("/api/auth/me", cookies)
            assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(res.body!!["username"]).isEqualTo("grace")
            assertThat(res.body!!["email"]).isEqualTo("grace@example.com")
            assertThat(res.body!!["emailVerified"]).isEqualTo(true)
        }

        @Test
        @DisplayName("POST /api/auth/refresh rotates the refresh_token and issues a new access_token")
        fun refresh_rotates_tokens() {
            registerAndVerify("henry@example.com", "henry")
            val loginRes = login("henry@example.com", validPassword)
            val oldAccess = cookiesOf(loginRes)["access_token"]!!
            val oldRefresh = cookiesOf(loginRes)["refresh_token"]!!

            // JWT `iat` is second-precision; delay slightly for a different access token
            Thread.sleep(1100)

            val refreshRes = postRaw("/api/auth/refresh", null, listOf("refresh_token=$oldRefresh"))
            assertThat(refreshRes.statusCode).isEqualTo(HttpStatus.OK)

            val newCookies = cookiesOf(refreshRes)
            assertThat(newCookies["refresh_token"]).isNotEqualTo(oldRefresh).isNotEmpty
            assertThat(newCookies["access_token"]).isNotEqualTo(oldAccess).isNotEmpty

            // Old refresh is now revoked and rejected
            val retry = postRaw("/api/auth/refresh", null, listOf("refresh_token=$oldRefresh"))
            assertThat(retry.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("POST /api/auth/refresh with no cookie returns 400")
        fun refresh_without_cookie() {
            val res = postRaw("/api/auth/refresh", null)
            assertThat(res.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("POST /api/auth/logout revokes the refresh token and clears cookies")
        fun logout_revokes_and_clears() {
            registerAndVerify("ivan@example.com", "ivan")
            val loginRes = login("ivan@example.com", validPassword)
            val refresh = cookiesOf(loginRes)["refresh_token"]!!

            val logoutRes = postRaw("/api/auth/logout", null, listOf("refresh_token=$refresh"))
            assertThat(logoutRes.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

            val setCookies = logoutRes.headers[HttpHeaders.SET_COOKIE] ?: emptyList()
            assertThat(setCookies.any { it.startsWith("access_token=") && it.contains("Max-Age=0") }).isTrue
            assertThat(setCookies.any { it.startsWith("refresh_token=") && it.contains("Max-Age=0") }).isTrue

            // DB state — the refresh token is revoked
            val stored = refreshTokenRepository.findAll().first()
            assertThat(stored.revoked).isTrue

            // Cannot reuse the revoked refresh
            val replay = postRaw("/api/auth/refresh", null, listOf("refresh_token=$refresh"))
            assertThat(replay.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    // ---------- Forgot / Reset -------------------------------------------------------

    @Nested
    @DisplayName("Forgot / Reset password")
    inner class ForgotReset {

        @Test
        @DisplayName("POST /api/auth/forgot-password with an unknown email still returns 200 (no enumeration)")
        fun forgot_no_enumeration() {
            val res = post("/api/auth/forgot-password", mapOf("email" to "nobody@example.com"))
            assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(mailCaptor.getToken("nobody@example.com")).isNull()
        }

        @Test
        @DisplayName("POST /api/auth/forgot-password with a known email captures a reset token")
        fun forgot_captures_token() {
            registerAndVerify("jane@example.com", "jane")
            val res = post("/api/auth/forgot-password", mapOf("email" to "jane@example.com"))
            assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(mailCaptor.getToken("jane@example.com")).isNotBlank
        }

        @Test
        @DisplayName("POST /api/auth/reset-password succeeds once and then rejects reuse of the token")
        fun reset_once_then_rejects_reuse() {
            registerAndVerify("kim@example.com", "kim")
            post("/api/auth/forgot-password", mapOf("email" to "kim@example.com"))
            val resetToken = mailCaptor.getToken("kim@example.com")!!

            val first = post(
                "/api/auth/reset-password",
                mapOf("token" to resetToken, "newPassword" to newValidPassword)
            )
            assertThat(first.statusCode).isEqualTo(HttpStatus.OK)

            val replay = post(
                "/api/auth/reset-password",
                mapOf("token" to resetToken, "newPassword" to newValidPassword)
            )
            assertThat(replay.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("POST /api/auth/reset-password with an invalid token returns 400")
        fun reset_invalid_token() {
            val res = post(
                "/api/auth/reset-password",
                mapOf("token" to "totally-invalid-token", "newPassword" to newValidPassword)
            )
            assertThat(res.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("after reset: old password rejected, new password works, all refresh tokens revoked")
        fun reset_flow_end_to_end() {
            registerAndVerify("leo@example.com", "leo")

            // Create two active refresh tokens
            assertThat(login("leo@example.com", validPassword).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(login("leo@example.com", validPassword).statusCode).isEqualTo(HttpStatus.OK)
            assertThat(refreshTokenRepository.findAll().count { !it.revoked }).isEqualTo(2)

            post("/api/auth/forgot-password", mapOf("email" to "leo@example.com"))
            val resetToken = mailCaptor.getToken("leo@example.com")!!

            val resetRes = post(
                "/api/auth/reset-password",
                mapOf("token" to resetToken, "newPassword" to newValidPassword)
            )
            assertThat(resetRes.statusCode).isEqualTo(HttpStatus.OK)

            // All refresh tokens revoked
            assertThat(refreshTokenRepository.findAll().all { it.revoked }).isTrue

            // Old password rejected
            assertThat(login("leo@example.com", validPassword).statusCode)
                .isEqualTo(HttpStatus.UNAUTHORIZED)

            // New password works
            assertThat(login("leo@example.com", newValidPassword).statusCode)
                .isEqualTo(HttpStatus.OK)
        }
    }
}

