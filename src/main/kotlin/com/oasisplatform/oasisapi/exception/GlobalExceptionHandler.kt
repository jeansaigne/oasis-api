package com.oasisplatform.oasisapi.exception

import com.oasisplatform.oasisapi.exception.auth.EmailNotVerifiedException
import com.oasisplatform.oasisapi.exception.auth.InvalidCredentialsException
import com.oasisplatform.oasisapi.exception.auth.InvalidTokenException
import com.oasisplatform.oasisapi.exception.auth.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val details: List<String>? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException::class)
    fun handleGameNotFound(ex: GameNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Jeu non trouvé"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Erreur de validation",
            details = details
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserExists(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Conflit"
            )
        )

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = ex.message ?: "Identifiants invalides"
            )
        )

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun handleEmailNotVerified(ex: EmailNotVerifiedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Email non confirmé",
                details = listOf("EMAIL_NOT_VERIFIED")
            )
        )

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Token invalide"
            )
        )

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Une erreur inattendue s'est produite"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
