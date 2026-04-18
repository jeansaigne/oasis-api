package com.oasisplatform.oasisapi.repository.auth

import com.oasisplatform.oasisapi.entity.auth.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>
    fun findByUsernameIgnoreCase(username: String): Optional<UserEntity>
    fun findByEmailIgnoreCaseOrUsernameIgnoreCase(email: String, username: String): Optional<UserEntity>
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByUsernameIgnoreCase(username: String): Boolean
}

