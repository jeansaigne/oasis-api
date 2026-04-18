package com.oasisplatform.oasisapi.support

import com.oasisplatform.oasisapi.mail.MailCaptor
import com.oasisplatform.oasisapi.repository.auth.EmailVerificationTokenRepository
import com.oasisplatform.oasisapi.repository.auth.PasswordResetTokenRepository
import com.oasisplatform.oasisapi.repository.auth.RefreshTokenRepository
import com.oasisplatform.oasisapi.repository.auth.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.DefaultResponseErrorHandler

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfig::class)
abstract class BaseIntegrationTest {

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var mailCaptor: MailCaptor

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var verificationRepository: EmailVerificationTokenRepository

    @Autowired
    protected lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    protected lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @BeforeEach
    fun baseSetUp() {
        val underlying = restTemplate.restTemplate
        // Don't follow redirects — assertions verify the 302 from /verify-email
        underlying.requestFactory = object : SimpleClientHttpRequestFactory() {
            override fun prepareConnection(connection: java.net.HttpURLConnection, httpMethod: String) {
                super.prepareConnection(connection, httpMethod)
                connection.instanceFollowRedirects = false
            }
        }
        // Don't throw on non-2xx so assertions can inspect the response
        underlying.errorHandler = object : DefaultResponseErrorHandler() {
            override fun hasError(statusCode: HttpStatusCode): Boolean = false
        }

        // Clean DB — order matters for FKs
        refreshTokenRepository.deleteAll()
        verificationRepository.deleteAll()
        passwordResetTokenRepository.deleteAll()
        userRepository.deleteAll()

        mailCaptor.clear()
    }
}
