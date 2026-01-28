package com.oasisplatform.oasisapi.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "games")
class Game(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(columnDefinition = "TEXT")
    var rules: String? = null,

    var minPlayers: Int? = null,

    var maxPlayers: Int? = null,

    var minAge: Int? = null,

    var durationMinutes: Int? = null,

    var author: String? = null,

    var releaseYear: Int? = null,

    var origin: String? = null,

    var imageUrl: String? = null,

    var rating: Double? = null,

    var isFavorite: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
