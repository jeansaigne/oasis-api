package com.oasisplatform.oasisapi.dto.camera

import com.oasisplatform.oasisapi.entity.camera.DetectionEvent
import java.time.Instant

data class DetectionEventResponse(
    val id: Long,
    val cameraId: String,
    val label: String,
    val count: Int,
    val confidence: Double,
    val detectedAt: Instant,
    val receivedAt: Instant
) {
    companion object {
        fun from(event: DetectionEvent): DetectionEventResponse = DetectionEventResponse(
            id = event.id!!,
            cameraId = event.cameraId,
            label = event.label,
            count = event.count,
            confidence = event.confidence,
            detectedAt = event.detectedAt,
            receivedAt = event.receivedAt
        )
    }
}
