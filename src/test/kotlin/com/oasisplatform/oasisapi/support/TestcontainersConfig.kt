package com.oasisplatform.oasisapi.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("oasis_test")
            .withUsername("oasis_test")
            .withPassword("oasis_test")
            .withReuse(true)

    /**
     * Replaces the real SMTP-backed JavaMailSender by a no-op to avoid any network call
     * or delay during tests. Token capture still happens via [com.oasisplatform.oasisapi.mail.MailCaptor].
     */
    @Bean
    @Primary
    fun noopMailSender(): JavaMailSender = object : JavaMailSenderImpl() {
        override fun send(vararg mimeMessages: jakarta.mail.internet.MimeMessage) = Unit
        override fun send(mimeMessage: jakarta.mail.internet.MimeMessage) = Unit
        override fun send(simpleMessage: org.springframework.mail.SimpleMailMessage) = Unit
        override fun send(vararg simpleMessages: org.springframework.mail.SimpleMailMessage) = Unit
    }
}

