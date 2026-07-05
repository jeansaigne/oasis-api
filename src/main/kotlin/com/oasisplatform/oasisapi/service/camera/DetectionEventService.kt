package com.oasisplatform.oasisapi.service.camera

import com.oasisplatform.oasisapi.dto.camera.DetectionEventRequest
import com.oasisplatform.oasisapi.dto.camera.DetectionEventResponse
import com.oasisplatform.oasisapi.entity.camera.DetectionEvent
import com.oasisplatform.oasisapi.repository.camera.DetectionEventRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DetectionEventService(
    private val detectionEventRepository: DetectionEventRepository,
    private val streamService: DetectionEventStreamService
) {

    fun create(request: DetectionEventRequest): DetectionEventResponse {
        val event = DetectionEvent(
            cameraId = request.cameraId,
            label = request.label,
            count = request.count,
            confidence = request.confidence,
            detectedAt = request.detectedAt
        )
        val response = DetectionEventResponse.from(detectionEventRepository.save(event))
        streamService.publish(response)
        return response
    }

    @Transactional(readOnly = true)
    fun findRecent(cameraId: String?, limit: Int): List<DetectionEventResponse> {
        val pageable = PageRequest.of(0, limit.coerceIn(1, MAX_LIMIT))
        val events = if (cameraId.isNullOrBlank()) {
            detectionEventRepository.findAllByOrderByDetectedAtDesc(pageable)
        } else {
            detectionEventRepository.findByCameraIdOrderByDetectedAtDesc(cameraId, pageable)
        }
        return events.map { DetectionEventResponse.from(it) }
    }

    companion object {
        private const val MAX_LIMIT = 500
    }
}
