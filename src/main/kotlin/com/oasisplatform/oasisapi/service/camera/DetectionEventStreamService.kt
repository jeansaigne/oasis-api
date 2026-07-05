package com.oasisplatform.oasisapi.service.camera

import com.oasisplatform.oasisapi.dto.camera.DetectionEventResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Broadcasts detection events to connected frontend clients over Server-Sent Events (SSE).
 *
 * SSE was chosen over WebSocket because the flow is strictly one-way (server -> browser),
 * it works with the plain `EventSource` browser API (auto-reconnect included) and it does
 * not require any extra Spring dependency.
 */
@Service
class DetectionEventStreamService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(EMITTER_TIMEOUT_MS)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        emitters.add(emitter)
        runCatching {
            emitter.send(SseEmitter.event().name("connected").data("ok"))
        }
        return emitter
    }

    fun publish(event: DetectionEventResponse) {
        val dead = mutableListOf<SseEmitter>()
        for (emitter in emitters) {
            try {
                emitter.send(SseEmitter.event().name("detection").data(event))
            } catch (ex: Exception) {
                logger.debug("Removing dead SSE emitter: {}", ex.message)
                dead.add(emitter)
            }
        }
        emitters.removeAll(dead)
    }

    fun subscriberCount(): Int = emitters.size

    companion object {
        /** 30 minutes; the browser EventSource reconnects automatically on timeout. */
        private const val EMITTER_TIMEOUT_MS = 30L * 60 * 1000
    }
}
