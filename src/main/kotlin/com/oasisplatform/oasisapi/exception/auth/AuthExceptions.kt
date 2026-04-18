package com.oasisplatform.oasisapi.exception.auth

class UserAlreadyExistsException(field: String) : RuntimeException("Un compte existe déjà avec ce $field")

class InvalidCredentialsException : RuntimeException("Identifiants invalides")

class EmailNotVerifiedException : RuntimeException("Veuillez confirmer votre adresse email avant de vous connecter")

class InvalidTokenException(message: String = "Token invalide ou expiré") : RuntimeException(message)

