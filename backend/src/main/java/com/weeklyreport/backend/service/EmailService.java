package com.weeklyreport.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends transactional email over SMTP. Knows nothing about why an email is sent. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String RESET_SUBJECT = "Reset your password";

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Best-effort send. A failure is logged and swallowed so the caller's HTTP response is
     * identical whether or not the email actually went out (avoids leaking account existence).
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(RESET_SUBJECT);
        message.setText(resetEmailBody(resetLink));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    private static String resetEmailBody(String resetLink) {
        return """
                We received a request to reset your password.

                Open this link to choose a new one:
                %s

                This link expires in 30 minutes. If you didn't request this, you can ignore this email.
                """
                .formatted(resetLink);
    }
}
