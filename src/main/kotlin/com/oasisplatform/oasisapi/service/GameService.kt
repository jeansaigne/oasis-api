package com.oasisplatform.oasisapi.service

import com.oasisplatform.oasisapi.dto.GameRequest
import com.oasisplatform.oasisapi.dto.GameResponse
import com.oasisplatform.oasisapi.entity.Game
import com.oasisplatform.oasisapi.exception.GameNotFoundException
import com.oasisplatform.oasisapi.repository.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService(private val gameRepository: GameRepository) {

    fun findAll(): List<GameResponse> =
        gameRepository.findAll().map { GameResponse.from(it) }

    fun findById(id: Long): GameResponse =
        gameRepository.findById(id)
            .map { GameResponse.from(it) }
            .orElseThrow { GameNotFoundException(id) }

    fun findFavorites(): List<GameResponse> =
        gameRepository.findByIsFavoriteTrue().map { GameResponse.from(it) }

    fun search(query: String): List<GameResponse> =
        gameRepository.searchByName(query).map { GameResponse.from(it) }

    fun findByPlayerRange(minPlayers: Int?, maxPlayers: Int?): List<GameResponse> =
        gameRepository.findByPlayerRange(minPlayers, maxPlayers).map { GameResponse.from(it) }

    fun create(request: GameRequest): GameResponse {
        val game = Game(
            name = request.name,
            description = request.description,
            rules = request.rules,
            minPlayers = request.minPlayers,
            maxPlayers = request.maxPlayers,
            minAge = request.minAge,
            durationMinutes = request.durationMinutes,
            author = request.author,
            releaseYear = request.releaseYear,
            origin = request.origin,
            imageUrl = request.imageUrl,
            rating = request.rating,
            isFavorite = request.isFavorite
        )
        return GameResponse.from(gameRepository.save(game))
    }

    fun update(id: Long, request: GameRequest): GameResponse {
        val game = gameRepository.findById(id)
            .orElseThrow { GameNotFoundException(id) }

        game.apply {
            name = request.name
            description = request.description
            rules = request.rules
            minPlayers = request.minPlayers
            maxPlayers = request.maxPlayers
            minAge = request.minAge
            durationMinutes = request.durationMinutes
            author = request.author
            releaseYear = request.releaseYear
            origin = request.origin
            imageUrl = request.imageUrl
            rating = request.rating
            isFavorite = request.isFavorite
        }

        return GameResponse.from(gameRepository.save(game))
    }

    fun delete(id: Long) {
        if (!gameRepository.existsById(id)) {
            throw GameNotFoundException(id)
        }
        gameRepository.deleteById(id)
    }
}
