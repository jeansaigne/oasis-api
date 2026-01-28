package com.oasisplatform.oasisapi.dto

import jakarta.validation.constraints.*

data class GameRequest(
    @field:NotBlank(message = "Le nom du jeu est obligatoire")
    @field:Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    val name: String,

    val description: String? = null,

    val rules: String? = null,

    @field:Min(value = 1, message = "Le nombre minimum de joueurs doit être au moins 1")
    val minPlayers: Int? = null,

    @field:Min(value = 1, message = "Le nombre maximum de joueurs doit être au moins 1")
    val maxPlayers: Int? = null,

    @field:Min(value = 0, message = "L'âge minimum ne peut pas être négatif")
    val minAge: Int? = null,

    @field:Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    val durationMinutes: Int? = null,

    val author: String? = null,

    @field:Min(value = 1800, message = "L'année de sortie doit être supérieure à 1800")
    val releaseYear: Int? = null,

    val origin: String? = null,

    val imageUrl: String? = null,

    @field:Min(value = 0, message = "La note ne peut pas être négative")
    @field:Max(value = 10, message = "La note ne peut pas dépasser 10")
    val rating: Double? = null,

    val isFavorite: Boolean = false
)
