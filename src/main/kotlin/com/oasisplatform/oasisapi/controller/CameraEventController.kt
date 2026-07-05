package com.oasisplatform.oasisapi.controller

import com.oasisplatform.oasisapi.dto.camera.DetectionEventRequest
import com.oasisplatform.oasisapi.dto.camera.DetectionEventResponse
import com.oasisplatform.oasisapi.exception.camera.InvalidCameraApiKeyException
import com.oasisplatform.oasisapi.service.camera.DetectionEventService
import com.oasisplatform.oasisapi.service.camera.DetectionEventStreamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/camera")
@Tag(name = "Camera", description = "Ingestion et diffusion des événements de détection caméra (OASIS Vision)")
class CameraEventController(
    private val detectionEventService: DetectionEventService,
    private val streamService: DetectionEventStreamService,
    @Value("\${app.camera.api-key:}") private val cameraApiKey: String
) {

    @PostMapping("/events")
    @Operation(
        summary = "Ingère un événement de détection",
        description = "Appelé par l'app iPhone OASIS Vision. Authentification par clé API " +
            "(header X-Api-Key, configurée via CAMERA_API_KEY côté serveur)."
    )
    fun ingestEvent(
        @RequestHeader(name = "X-Api-Key", required = false) apiKey: String?,
        @Valid @RequestBody request: DetectionEventRequest
    ): ResponseEntity<DetectionEventResponse> {
        if (cameraApiKey.isBlank() || apiKey != cameraApiKey) {
            throw InvalidCameraApiKeyException()
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(detectionEventService.create(request))
    }

    @GetMapping("/events")
    @Operation(summary = "Liste les derniers événements de détection (plus récents en premier)")
    fun getRecentEvents(
        @RequestParam(required = false) cameraId: String?,
        @RequestParam(required = false, defaultValue = "50") limit: Int
    ): ResponseEntity<List<DetectionEventResponse>> =
        ResponseEntity.ok(detectionEventService.findRecent(cameraId, limit))

    @GetMapping("/events/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(
        summary = "Flux SSE des événements de détection en temps réel",
        description = "À consommer avec EventSource côté navigateur. Événements nommés 'detection'."
    )
    fun streamEvents(): SseEmitter = streamService.subscribe()
}
