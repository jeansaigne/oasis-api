package com.oasisplatform.oasisapi.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

private const val PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
private const val PASSWORD_MESSAGE =
    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial"

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    @field:Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Nom d'utilisateur invalide")
    val username: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val email: String,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    val password: String
)

data class LoginRequest(
    @field:NotBlank
    val identifier: String,

    @field:NotBlank
    val password: String
)

data class ForgotPasswordRequest(
    @field:NotBlank
    @field:Email
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,

    @field:NotBlank
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    val newPassword: String
)

data class MessageResponse(val message: String)

