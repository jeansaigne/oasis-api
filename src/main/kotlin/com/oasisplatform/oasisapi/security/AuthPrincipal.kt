package com.oasisplatform.oasisapi.security

import java.util.UUID

/** Principal stored in the SecurityContext after JWT authentication. */
data class AuthPrincipal(
    val id: UUID,
    val username: String
)

