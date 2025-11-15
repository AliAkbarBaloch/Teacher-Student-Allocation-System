package de.unipassau.allocationsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 * Supports both actual email sending via SMTP and logging mode for development.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.name:Allocation System}")
    private String appName;

    @Value("${app.email.from:noreply@allocationsystem.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Send password reset email.
     * 
     * @param toEmail recipient email
     * @param resetToken password reset token
     * @param userName user's full name
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken, String userName) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        String subject = appName + " - Password Reset Request";
        String body = buildPasswordResetEmailBody(userName, resetLink);
        
        sendEmail(toEmail, subject, body, resetToken);
    }

    /**
     * Send welcome email to new user.
     * 
     * @param toEmail recipient email
     * @param userName user's full name
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to " + appName;
        String body = buildWelcomeEmailBody(userName);
        
        sendEmail(toEmail, subject, body, null);
    }

    /**
     * Send account locked notification.
     * 
     * @param toEmail recipient email
     * @param userName user's full name
     */
    public void sendAccountLockedEmail(String toEmail, String userName) {
        String subject = appName + " - Account Locked";
        String body = buildAccountLockedEmailBody(userName);
        
        sendEmail(toEmail, subject, body, null);
    }

    /**
     * Internal method to send email - uses actual SMTP if enabled, otherwise logs.
     * 
     * @param toEmail recipient email
     * @param subject email subject
     * @param body email body
     * @param token optional token (for password reset)
     */
    private void sendEmail(String toEmail, String subject, String body, String token) {
        if (emailEnabled) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                
                mailSender.send(message);
                log.info("Email sent successfully to: {}", toEmail);
                
            } catch (MailException e) {
                log.error("Failed to send email to: {}. Error: {}", toEmail, e.getMessage());
                // Fall back to logging
                logEmail(toEmail, subject, body, token);
            }
        } else {
            // Email disabled - log instead
            logEmail(toEmail, subject, body, token);
        }
    }

    /**
     * Log email content when actual sending is disabled or fails.
     */
    private void logEmail(String toEmail, String subject, String body, String token) {
        log.info("=== EMAIL (Logging Mode) ===");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        if (token != null) {
            log.info("Token: {}", token);
        }
        log.info("===========================");
    }

    private String buildPasswordResetEmailBody(String userName, String resetLink) {
        return String.format("""
                Hello %s,
                
                We received a request to reset your password for your %s account.
                
                Click the link below to reset your password:
                %s
                
                This link will expire in 24 hours.
                
                If you didn't request a password reset, please ignore this email or contact support if you have concerns.
                
                Best regards,
                The %s Team
                """, userName, appName, resetLink, appName);
    }

    private String buildWelcomeEmailBody(String userName) {
        return String.format("""
                Hello %s,
                
                Welcome to %s!
                
                Your account has been successfully created. You can now log in using your email and password.
                
                If you have any questions, please don't hesitate to contact our support team.
                
                Best regards,
                The %s Team
                """, userName, appName, appName);
    }

    private String buildAccountLockedEmailBody(String userName) {
        return String.format("""
                Hello %s,
                
                Your %s account has been temporarily locked due to multiple failed login attempts.
                
                For security reasons, your account has been locked. If this was you, please reset your password.
                If you believe this was unauthorized access, please contact our support team immediately.
                
                You can reset your password to unlock your account.
                
                Best regards,
                The %s Team
                """, userName, appName, appName);
    }
}
