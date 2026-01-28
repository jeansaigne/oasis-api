package com.oasisplatform.oasisapi.repository

import com.oasisplatform.oasisapi.entity.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : JpaRepository<Game, Long> {

    fun findByIsFavoriteTrue(): List<Game>

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    fun searchByName(search: String): List<Game>

    @Query("""
        SELECT g FROM Game g
        WHERE (:minPlayers IS NULL OR g.minPlayers >= :minPlayers)
        AND (:maxPlayers IS NULL OR g.maxPlayers <= :maxPlayers)
    """)
    fun findByPlayerRange(minPlayers: Int?, maxPlayers: Int?): List<Game>
}
