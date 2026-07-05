package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.dto.camera.DetectionEventRequest
import com.oasisplatform.oasisapi.entity.camera.DetectionEvent
import com.oasisplatform.oasisapi.repository.camera.DetectionEventRepository
import com.oasisplatform.oasisapi.support.TestcontainersConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
class CameraEventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var detectionEventRepository: DetectionEventRepository

    private val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    /** Matches app.camera.api-key in application-test.yml. */
    private val apiKey = "test-camera-api-key"

    @BeforeEach
    fun setUp() {
        detectionEventRepository.deleteAll()
    }

    private fun request(
        label: String = "person",
        count: Int = 1,
        confidence: Double = 0.87,
        cameraId: String = "iphone-11-test"
    ) = DetectionEventRequest(
        cameraId = cameraId,
        label = label,
        count = count,
        confidence = confidence,
        detectedAt = Instant.parse("2026-07-05T12:00:00Z")
    )

    @Test
    fun `should ingest a detection event with a valid API key`() {
        mockMvc.perform(
            post("/api/camera/events")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request()))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.label").value("person"))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.confidence").value(0.87))
            .andExpect(jsonPath("$.cameraId").value("iphone-11-test"))

        assert(detectionEventRepository.count() == 1L)
    }

    @Test
    fun `should reject a detection event with a wrong API key`() {
        mockMvc.perform(
            post("/api/camera/events")
                .header("X-Api-Key", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request()))
        )
            .andExpect(status().isUnauthorized)

        assert(detectionEventRepository.count() == 0L)
    }

    @Test
    fun `should reject a detection event without API key`() {
        mockMvc.perform(
            post("/api/camera/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject an invalid detection event payload`() {
        mockMvc.perform(
            post("/api/camera/events")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request(label = "", confidence = 1.5)))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should list recent detection events most recent first`() {
        detectionEventRepository.save(
            DetectionEvent(
                cameraId = "iphone-11-test", label = "cat", count = 1, confidence = 0.6,
                detectedAt = Instant.parse("2026-07-05T10:00:00Z")
            )
        )
        detectionEventRepository.save(
            DetectionEvent(
                cameraId = "iphone-11-test", label = "person", count = 2, confidence = 0.9,
                detectedAt = Instant.parse("2026-07-05T11:00:00Z")
            )
        )

        mockMvc.perform(get("/api/camera/events").param("limit", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].label").value("person"))
            .andExpect(jsonPath("$[1].label").value("cat"))
    }

    @Test
    fun `should filter detection events by cameraId`() {
        detectionEventRepository.save(
            DetectionEvent(
                cameraId = "camera-a", label = "dog", count = 1, confidence = 0.7,
                detectedAt = Instant.parse("2026-07-05T10:00:00Z")
            )
        )
        detectionEventRepository.save(
            DetectionEvent(
                cameraId = "camera-b", label = "car", count = 1, confidence = 0.8,
                detectedAt = Instant.parse("2026-07-05T11:00:00Z")
            )
        )

        mockMvc.perform(get("/api/camera/events").param("cameraId", "camera-a"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].label").value("dog"))
    }

    @Test
    fun `should open an SSE stream`() {
        mockMvc.perform(get("/api/camera/events/stream"))
            .andExpect(status().isOk)
    }
}
