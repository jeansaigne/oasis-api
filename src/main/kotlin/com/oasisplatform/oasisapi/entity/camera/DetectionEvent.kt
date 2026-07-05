package com.oasisplatform.oasisapi.entity.camera

import jakarta.persistence.*
import java.time.Instant

/**
 * A single object-detection event reported by a camera device
 * (e.g. the OASIS Vision iPhone app running YOLO on-device).
 */
@Entity
@Table(name = "detection_events")
class DetectionEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /** Logical identifier of the camera that produced the event (e.g. "iphone-11-salon"). */
    @Column(nullable = false, length = 100)
    var cameraId: String,

    /** Detected class label (COCO), e.g. "person", "cat", "car". */
    @Column(nullable = false, length = 100)
    var label: String,

    /** Number of objects of this label detected in the frame. */
    @Column(nullable = false)
    var count: Int,

    /** Highest confidence among the detected objects of this label (0.0–1.0). */
    @Column(nullable = false)
    var confidence: Double,

    /** Moment the detection happened, as reported by the device. */
    @Column(nullable = false)
    var detectedAt: Instant,

    /** Moment the event was received by the server. */
    @Column(nullable = false, updatable = false)
    var receivedAt: Instant = Instant.now()
)
