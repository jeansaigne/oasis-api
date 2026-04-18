package com.oasisplatform.oasisapi.security

/*
 * Scaffold for future OAuth2 provider integration (Google, GitHub, ...).
 *
 * When enabling OAuth2Login:
 *  - register this as a @Component
 *  - make it implement AuthenticationSuccessHandler
 *  - on success: lookup or create a UserEntity by email (email_verified = true),
 *                issue JWT access + refresh tokens like in AuthService.login,
 *                set them as HttpOnly cookies via CookieService,
 *                then redirect to ${app.frontend.base-url}.
 *  - wire it in SecurityConfig: http.oauth2Login { it.successHandler(oauth2LoginSuccessHandler) }
 */
// class OAuth2LoginSuccessHandler : AuthenticationSuccessHandler { ... }

