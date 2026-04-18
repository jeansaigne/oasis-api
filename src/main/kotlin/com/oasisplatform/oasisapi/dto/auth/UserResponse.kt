package com.oasisplatform.oasisapi.dto.auth

import com.oasisplatform.oasisapi.entity.auth.UserEntity
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val emailVerified: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: UserEntity): UserResponse = UserResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            emailVerified = user.emailVerified,
            createdAt = user.createdAt
        )
    }
}

