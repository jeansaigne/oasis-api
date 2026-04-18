package com.oasisplatform.oasisapi.mail

import com.oasisplatform.oasisapi.entity.auth.UserEntity
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.mail.from}") private val from: String,
    @Value("\${app.mail.from-name}") private val fromName: String,
    @Value("\${app.frontend.base-url}") private val frontendBaseUrl: String,
    @Value("\${app.api.base-url}") private val apiBaseUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(user: UserEntity, token: String) {
        val link = "$apiBaseUrl/api/auth/verify-email?token=$token"
        val subject = "Confirmez votre adresse email — Oasis"
        val html = """
            <!doctype html>
            <html lang="fr"><body style="font-family: system-ui, sans-serif; background:#0b0b12; color:#e5e7eb; padding:24px;">
              <div style="max-width:560px;margin:0 auto;background:#14141f;border:1px solid #2d2d44;border-radius:12px;padding:24px;">
                <h1 style="color:#ff0080;margin-top:0;">Bienvenue sur Oasis ✨</h1>
                <p>Bonjour <strong>${user.username}</strong>,</p>
                <p>Merci de confirmer votre adresse email en cliquant sur le bouton ci-dessous :</p>
                <p style="text-align:center;margin:32px 0;">
                  <a href="$link" style="background:linear-gradient(90deg,#ff0080,#00cfff);color:#fff;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;">Confirmer mon email</a>
                </p>
                <p style="font-size:12px;color:#9ca3af;">Ou copiez ce lien : <br/>$link</p>
                <p style="font-size:12px;color:#9ca3af;">Ce lien expire dans 24 heures.</p>
              </div>
            </body></html>
        """.trimIndent()
        send(user.email, subject, html)
    }

    fun sendPasswordResetEmail(user: UserEntity, rawToken: String) {
        val link = "$frontendBaseUrl/reset-password?token=$rawToken"
        val subject = "Réinitialisation de votre mot de passe — Oasis"
        val html = """
            <!doctype html>
            <html lang="fr"><body style="font-family: system-ui, sans-serif; background:#0b0b12; color:#e5e7eb; padding:24px;">
              <div style="max-width:560px;margin:0 auto;background:#14141f;border:1px solid #2d2d44;border-radius:12px;padding:24px;">
                <h1 style="color:#00cfff;margin-top:0;">Réinitialisation de mot de passe</h1>
                <p>Bonjour <strong>${user.username}</strong>,</p>
                <p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton pour en choisir un nouveau :</p>
                <p style="text-align:center;margin:32px 0;">
                  <a href="$link" style="background:linear-gradient(90deg,#8000ff,#00cfff);color:#fff;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;">Réinitialiser mon mot de passe</a>
                </p>
                <p style="font-size:12px;color:#9ca3af;">Ou copiez ce lien : <br/>$link</p>
                <p style="font-size:12px;color:#9ca3af;">Ce lien expire dans 1 heure. Si vous n'avez pas fait cette demande, ignorez cet email.</p>
              </div>
            </body></html>
        """.trimIndent()
        send(user.email, subject, html)
    }

    private fun send(to: String, subject: String, htmlBody: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, "UTF-8")
            helper.setFrom(InternetAddress(from, fromName))
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)
            mailSender.send(message)
        } catch (ex: Exception) {
            // Do not leak SMTP failures to the client; log for ops
            log.error("Failed to send email to {}: {}", to, ex.message)
        }
    }
}

