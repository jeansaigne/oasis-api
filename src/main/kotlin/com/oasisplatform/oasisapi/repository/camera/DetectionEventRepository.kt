package com.oasisplatform.oasisapi.repository.camera

import com.oasisplatform.oasisapi.entity.camera.DetectionEvent
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DetectionEventRepository : JpaRepository<DetectionEvent, Long> {

    fun findAllByOrderByDetectedAtDesc(pageable: Pageable): List<DetectionEvent>

    fun findByCameraIdOrderByDetectedAtDesc(cameraId: String, pageable: Pageable): List<DetectionEvent>
}
