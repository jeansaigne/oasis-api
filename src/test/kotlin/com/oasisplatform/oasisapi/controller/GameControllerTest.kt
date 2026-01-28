package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.dto.GameRequest
import com.oasisplatform.oasisapi.entity.Game
import com.oasisplatform.oasisapi.repository.GameRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var gameRepository: GameRepository

    private val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    @BeforeEach
    fun setup() {
        gameRepository.deleteAll()
    }

    @Test
    fun `should create a new game`() {
        val request = GameRequest(
            name = "Monopoly",
            description = "Jeu de société économique",
            minPlayers = 2,
            maxPlayers = 8,
            minAge = 8,
            durationMinutes = 120
        )

        mockMvc.perform(
            post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Monopoly"))
            .andExpect(jsonPath("$.minPlayers").value(2))
            .andExpect(jsonPath("$.maxPlayers").value(8))
    }

    @Test
    fun `should return 400 when name is blank`() {
        val request = GameRequest(name = "")

        mockMvc.perform(
            post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should get all games`() {
        gameRepository.save(Game(name = "Monopoly"))
        gameRepository.save(Game(name = "Scrabble"))

        mockMvc.perform(get("/api/games"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should get game by id`() {
        val game = gameRepository.save(Game(name = "Monopoly", minPlayers = 2))

        mockMvc.perform(get("/api/games/${game.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Monopoly"))
            .andExpect(jsonPath("$.minPlayers").value(2))
    }

    @Test
    fun `should return 404 when game not found`() {
        mockMvc.perform(get("/api/games/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update a game`() {
        val game = gameRepository.save(Game(name = "Monopoly"))
        val request = GameRequest(name = "Monopoly Updated", minPlayers = 3)

        mockMvc.perform(
            put("/api/games/${game.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Monopoly Updated"))
            .andExpect(jsonPath("$.minPlayers").value(3))
    }

    @Test
    fun `should delete a game`() {
        val game = gameRepository.save(Game(name = "Monopoly"))

        mockMvc.perform(delete("/api/games/${game.id}"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/games/${game.id}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should search games by name`() {
        gameRepository.save(Game(name = "Monopoly"))
        gameRepository.save(Game(name = "Scrabble"))

        mockMvc.perform(get("/api/games?search=mono"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Monopoly"))
    }

    @Test
    fun `should get favorite games`() {
        gameRepository.save(Game(name = "Monopoly", isFavorite = true))
        gameRepository.save(Game(name = "Scrabble", isFavorite = false))

        mockMvc.perform(get("/api/games?favorites=true"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Monopoly"))
    }
}
