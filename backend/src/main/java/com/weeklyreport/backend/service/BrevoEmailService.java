package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Sends transactional email through Brevo's REST API. Knows nothing about why an email is sent. */
@Service
public class BrevoEmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);

    private static final String SEND_EMAIL_PATH = "/smtp/email";
    private static final String SENDER_NAME = "Weekly Report Generator";
    private static final String RESET_SUBJECT = "Reset your password";

    private final RestClient restClient;
    private final String senderEmail;

    public BrevoEmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.sender-email}") String senderEmail) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
        this.senderEmail = senderEmail;
    }

    /**
     * Best-effort send. A failure is logged and swallowed so the caller's HTTP response is
     * identical whether or not the email actually went out (avoids leaking account existence).
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            restClient
                    .post()
                    .uri(SEND_EMAIL_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resetEmailPayload(toEmail, resetLink))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    private Map<String, Object> resetEmailPayload(String toEmail, String resetLink) {
        return Map.of(
                "sender", Map.of("name", SENDER_NAME, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", RESET_SUBJECT,
                "htmlContent", resetEmailBody(resetLink));
    }

    private static String resetEmailBody(String resetLink) {
        return """
                <p>We received a request to reset your password.</p>
                <p><a href="%s">Reset your password</a></p>
                <p>This link expires in 30 minutes. If you didn't request this, you can ignore this email.</p>
                """
                .formatted(resetLink);
    }
}
