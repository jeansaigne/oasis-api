package com.oasisplatform.oasisapi.service.auth

import com.oasisplatform.oasisapi.dto.auth.LoginRequest
import com.oasisplatform.oasisapi.dto.auth.RegisterRequest
import com.oasisplatform.oasisapi.dto.auth.ResetPasswordRequest
import com.oasisplatform.oasisapi.dto.auth.UserResponse
import com.oasisplatform.oasisapi.entity.auth.EmailVerificationTokenEntity
import com.oasisplatform.oasisapi.entity.auth.PasswordResetTokenEntity
import com.oasisplatform.oasisapi.entity.auth.UserEntity
import com.oasisplatform.oasisapi.exception.auth.EmailNotVerifiedException
import com.oasisplatform.oasisapi.exception.auth.InvalidCredentialsException
import com.oasisplatform.oasisapi.exception.auth.InvalidTokenException
import com.oasisplatform.oasisapi.exception.auth.UserAlreadyExistsException
import com.oasisplatform.oasisapi.mail.MailCaptor
import com.oasisplatform.oasisapi.mail.MailService
import com.oasisplatform.oasisapi.repository.auth.EmailVerificationTokenRepository
import com.oasisplatform.oasisapi.repository.auth.PasswordResetTokenRepository
import com.oasisplatform.oasisapi.repository.auth.UserRepository
import com.oasisplatform.oasisapi.security.JwtService
import com.oasisplatform.oasisapi.security.RefreshTokenService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.HexFormat
import java.util.UUID

data class LoginResult(
    val user: UserEntity,
    val accessToken: String,
    val refreshToken: String
)

data class RefreshResult(
    val accessToken: String,
    val refreshToken: String,
    val user: UserEntity
)

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val verificationRepository: EmailVerificationTokenRepository,
    private val resetRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val mailService: MailService,
    mailCaptorProvider: ObjectProvider<MailCaptor>
) {
    private val random = SecureRandom()
    private val mailCaptor: MailCaptor? = mailCaptorProvider.ifAvailable

    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmailIgnoreCase(request.email)) {
            throw UserAlreadyExistsException("email")
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username)) {
            throw UserAlreadyExistsException("nom d'utilisateur")
        }

        val user = userRepository.save(
            UserEntity(
                username = request.username,
                email = request.email.lowercase(),
                passwordHash = passwordEncoder.encode(request.password),
                emailVerified = false,
                enabled = true
            )
        )

        val token = generateUrlSafeToken()
        verificationRepository.save(
            EmailVerificationTokenEntity(
                user = user,
                token = token,
                expiresAt = LocalDateTime.now().plusHours(24)
            )
        )
        mailCaptor?.capture(user.email, token)
        mailService.sendVerificationEmail(user, token)
        return UserResponse.from(user)
    }

    fun verifyEmail(token: String) {
        val entity = verificationRepository.findByToken(token)
            .orElseThrow { InvalidTokenException("Token de confirmation invalide") }
        if (entity.used || entity.expiresAt.isBefore(LocalDateTime.now())) {
            throw InvalidTokenException("Token de confirmation expiré ou déjà utilisé")
        }
        entity.used = true
        val user = entity.user
        user.emailVerified = true
        userRepository.save(user)
        verificationRepository.save(entity)
    }

    fun login(request: LoginRequest): LoginResult {
        val user = userRepository
            .findByEmailIgnoreCaseOrUsernameIgnoreCase(request.identifier, request.identifier)
            .orElseThrow { InvalidCredentialsException() }

        val hash = user.passwordHash ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, hash)) {
            throw InvalidCredentialsException()
        }
        if (!user.enabled) throw InvalidCredentialsException()
        if (!user.emailVerified) throw EmailNotVerifiedException()

        val access = jwtService.generateAccessToken(user.id!!, user.username)
        val refresh = refreshTokenService.issue(user)
        return LoginResult(user, access, refresh)
    }

    fun refresh(rawRefreshToken: String?): RefreshResult {
        if (rawRefreshToken.isNullOrBlank()) throw InvalidTokenException("Refresh token manquant")
        val entity = refreshTokenService.findValid(rawRefreshToken)
            ?: throw InvalidTokenException("Refresh token invalide")
        // Rotation
        refreshTokenService.revoke(entity)
        val user = entity.user
        val newRefresh = refreshTokenService.issue(user)
        val newAccess = jwtService.generateAccessToken(user.id!!, user.username)
        return RefreshResult(newAccess, newRefresh, user)
    }

    fun logout(rawRefreshToken: String?) {
        refreshTokenService.revokeIfPresent(rawRefreshToken)
    }

    @Transactional(readOnly = true)
    fun me(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException() }
        return UserResponse.from(user)
    }

    fun forgotPassword(email: String) {
        val user = userRepository.findByEmailIgnoreCase(email).orElse(null) ?: return
        val raw = generateUrlSafeToken()
        resetRepository.save(
            PasswordResetTokenEntity(
                user = user,
                tokenHash = refreshTokenService.hash(raw),
                expiresAt = LocalDateTime.now().plusHours(1)
            )
        )
        mailCaptor?.capture(user.email, raw)
        mailService.sendPasswordResetEmail(user, raw)
    }

    fun resetPassword(request: ResetPasswordRequest) {
        val hashed = refreshTokenService.hash(request.token)
        val entity = resetRepository.findByTokenHash(hashed)
            .orElseThrow { InvalidTokenException("Lien de réinitialisation invalide") }
        if (entity.used || entity.expiresAt.isBefore(LocalDateTime.now())) {
            throw InvalidTokenException("Lien de réinitialisation expiré ou déjà utilisé")
        }
        val user = entity.user
        user.passwordHash = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
        entity.used = true
        resetRepository.save(entity)
        refreshTokenService.revokeAllForUser(user)
    }

    private fun generateUrlSafeToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return HexFormat.of().formatHex(bytes)
    }
}

