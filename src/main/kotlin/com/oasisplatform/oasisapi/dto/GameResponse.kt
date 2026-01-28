package com.oasisplatform.oasisapi.dto

import com.oasisplatform.oasisapi.entity.Game
import java.time.LocalDateTime

data class GameResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val rules: String?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val minAge: Int?,
    val durationMinutes: Int?,
    val author: String?,
    val releaseYear: Int?,
    val origin: String?,
    val imageUrl: String?,
    val rating: Double?,
    val isFavorite: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(game: Game): GameResponse = GameResponse(
            id = game.id!!,
            name = game.name,
            description = game.description,
            rules = game.rules,
            minPlayers = game.minPlayers,
            maxPlayers = game.maxPlayers,
            minAge = game.minAge,
            durationMinutes = game.durationMinutes,
            author = game.author,
            releaseYear = game.releaseYear,
            origin = game.origin,
            imageUrl = game.imageUrl,
            rating = game.rating,
            isFavorite = game.isFavorite,
            createdAt = game.createdAt,
            updatedAt = game.updatedAt
        )
    }
}
