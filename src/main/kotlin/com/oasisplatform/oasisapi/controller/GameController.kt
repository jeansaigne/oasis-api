package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.dto.GameRequest
import com.oasisplatform.oasisapi.dto.GameResponse
import com.oasisplatform.oasisapi.service.GameService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
@Tag(name = "Games", description = "API pour la gestion des jeux")
class GameController(private val gameService: GameService) {

    @GetMapping
    @Operation(summary = "Liste tous les jeux", description = "Récupère la liste de tous les jeux avec possibilité de filtrage")
    fun getAllGames(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) minPlayers: Int?,
        @RequestParam(required = false) maxPlayers: Int?,
        @RequestParam(required = false) favorites: Boolean?
    ): ResponseEntity<List<GameResponse>> {
        val games = when {
            favorites == true -> gameService.findFavorites()
            !search.isNullOrBlank() -> gameService.search(search)
            minPlayers != null || maxPlayers != null -> gameService.findByPlayerRange(minPlayers, maxPlayers)
            else -> gameService.findAll()
        }
        return ResponseEntity.ok(games)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un jeu par son ID")
    fun getGameById(@PathVariable id: Long): ResponseEntity<GameResponse> =
        ResponseEntity.ok(gameService.findById(id))

    @PostMapping
    @Operation(summary = "Crée un nouveau jeu")
    fun createGame(@Valid @RequestBody request: GameRequest): ResponseEntity<GameResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(gameService.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un jeu existant")
    fun updateGame(
        @PathVariable id: Long,
        @Valid @RequestBody request: GameRequest
    ): ResponseEntity<GameResponse> =
        ResponseEntity.ok(gameService.update(id, request))

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un jeu")
    fun deleteGame(@PathVariable id: Long): ResponseEntity<Void> {
        gameService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
