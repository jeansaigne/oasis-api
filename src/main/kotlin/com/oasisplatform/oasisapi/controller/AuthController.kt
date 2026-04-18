package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.dto.auth.ForgotPasswordRequest
import com.oasisplatform.oasisapi.dto.auth.LoginRequest
import com.oasisplatform.oasisapi.dto.auth.MessageResponse
import com.oasisplatform.oasisapi.dto.auth.RegisterRequest
import com.oasisplatform.oasisapi.dto.auth.ResetPasswordRequest
import com.oasisplatform.oasisapi.dto.auth.UserResponse
import com.oasisplatform.oasisapi.exception.auth.InvalidCredentialsException
import com.oasisplatform.oasisapi.security.AuthPrincipal
import com.oasisplatform.oasisapi.security.CookieService
import com.oasisplatform.oasisapi.service.auth.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification et gestion de comptes")
class AuthController(
    private val authService: AuthService,
    private val cookieService: CookieService,
    @Value("\${app.frontend.base-url}") private val frontendBaseUrl: String
) {

    @PostMapping("/register")
    @Operation(summary = "Inscription — envoie un email de confirmation")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @GetMapping("/verify-email")
    @Operation(summary = "Valide un token de confirmation d'email et redirige vers le frontend")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<Void> {
        return try {
            authService.verifyEmail(token)
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("$frontendBaseUrl/login?verified=true"))
                .build()
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("$frontendBaseUrl/login?verified=false"))
                .build()
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion — pose les cookies access_token et refresh_token")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val result = authService.login(request)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookieService.buildAccessCookie(result.accessToken).toString())
        headers.add(HttpHeaders.SET_COOKIE, cookieService.buildRefreshCookie(result.refreshToken).toString())
        return ResponseEntity.ok().headers(headers).body(UserResponse.from(result.user))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotation du refresh token — émet un nouveau access_token")
    fun refresh(request: HttpServletRequest): ResponseEntity<MessageResponse> {
        val raw = request.cookies?.firstOrNull { it.name == CookieService.REFRESH_COOKIE }?.value
        val result = authService.refresh(raw)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookieService.buildAccessCookie(result.accessToken).toString())
        headers.add(HttpHeaders.SET_COOKIE, cookieService.buildRefreshCookie(result.refreshToken).toString())
        return ResponseEntity.ok().headers(headers).body(MessageResponse("OK"))
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion — révoque le refresh token et supprime les cookies")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val raw = request.cookies?.firstOrNull { it.name == CookieService.REFRESH_COOKIE }?.value
        authService.logout(raw)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookieService.clearAccessCookie().toString())
        headers.add(HttpHeaders.SET_COOKIE, cookieService.clearRefreshCookie().toString())
        return ResponseEntity.noContent().headers(headers).build()
    }

    @GetMapping("/me")
    @Operation(summary = "Retourne l'utilisateur connecté")
    fun me(@AuthenticationPrincipal principal: AuthPrincipal?): ResponseEntity<UserResponse> {
        val p = principal ?: throw InvalidCredentialsException()
        return ResponseEntity.ok(authService.me(p.id))
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation — retourne toujours 200")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        authService.forgotPassword(request.email)
        return ResponseEntity.ok(MessageResponse("Si cet email existe, un lien de réinitialisation vous a été envoyé."))
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialise le mot de passe à partir d'un token")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        authService.resetPassword(request)
        return ResponseEntity.ok(MessageResponse("Mot de passe mis à jour."))
    }
}

