package com.oasisplatform.oasisapi.dto.camera

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class DetectionEventRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val cameraId: String,

    @field:NotBlank
    @field:Size(max = 100)
    val label: String,

    @field:Min(1)
    val count: Int,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val confidence: Double,

    @field:NotNull
    val detectedAt: Instant
)
