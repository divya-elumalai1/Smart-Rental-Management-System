package com.smartrental.service;

import com.smartrental.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Email service for sending transactional emails.
 * Supports both simple text and HTML templated emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // ===========================================
    // Verification Emails
    // ===========================================

    /**
     * Send email verification link.
     * @param user the user
     * @param token verification token
     */
    @Async
    public void sendVerificationEmail(User user, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("verificationLink", verificationLink);
        variables.put("expiryHours", 24);

        sendTemplatedEmail(
                user.getEmail(),
                "Verify Your Email - Smart Rental Management",
                "email/verification",
                variables
        );
    }

    /**
     * Send welcome email after email verification.
     * @param user the user
     */
    @Async
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("role", user.getRole().getDisplayName());
        variables.put("dashboardLink", frontendUrl + "/dashboard");

        sendTemplatedEmail(
                user.getEmail(),
                "Welcome to Smart Rental Management!",
                "email/welcome",
                variables
        );
    }

    // ===========================================
    // Password Reset Emails
    // ===========================================

    /**
     * Send password reset link.
     * @param user the user
     * @param token reset token
     */
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("resetLink", resetLink);
        variables.put("expiryMinutes", 60);

        sendTemplatedEmail(
                user.getEmail(),
                "Password Reset Request - Smart Rental Management",
                "email/password-reset",
                variables
        );
    }

    /**
     * Send password changed confirmation.
     * @param user the user
     */
    @Async
    public void sendPasswordChangedConfirmation(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("changedAt", java.time.LocalDateTime.now().toString());

        sendTemplatedEmail(
                user.getEmail(),
                "Password Changed Successfully - Smart Rental Management",
                "email/password-changed",
                variables
        );
    }

    // ===========================================
    // Rent Payment Emails
    // ===========================================

    /**
     * Send rent payment reminder.
     * @param user the tenant
     * @param amount amount due
     * @param dueDate due date
     * @param propertyName property name
     * @param paymentLink payment link
     */
    @Async
    public void sendRentReminderEmail(User user, double amount, java.time.LocalDate dueDate,
                                       String propertyName, String paymentLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("amount", String.format("%.2f", amount));
        variables.put("dueDate", dueDate.toString());
        variables.put("propertyName", propertyName);
        variables.put("paymentLink", paymentLink);

        sendTemplatedEmail(
                user.getEmail(),
                "Rent Payment Reminder - Smart Rental Management",
                "email/rent-reminder",
                variables
        );
    }

    /**
     * Send rent payment confirmation.
     * @param user the tenant
     * @param amount amount paid
     * @param propertyName property name
     * @param transactionId transaction ID
     */
    @Async
    public void sendPaymentConfirmationEmail(User user, double amount, String propertyName, String transactionId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("amount", String.format("%.2f", amount));
        variables.put("propertyName", propertyName);
        variables.put("transactionId", transactionId);
        variables.put("paidAt", java.time.LocalDateTime.now().toString());

        sendTemplatedEmail(
                user.getEmail(),
                "Rent Payment Confirmation - Smart Rental Management",
                "email/payment-confirmation",
                variables
        );
    }

    /**
     * Send overdue rent notice.
     * @param user the tenant
     * @param amount amount overdue
     * @param dueDate original due date
     * @param propertyName property name
     * @param daysOverdue days overdue
     */
    @Async
    public void sendOverdueNoticeEmail(User user, double amount, java.time.LocalDate dueDate,
                                        String propertyName, int daysOverdue) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("amount", String.format("%.2f", amount));
        variables.put("dueDate", dueDate.toString());
        variables.put("propertyName", propertyName);
        variables.put("daysOverdue", daysOverdue);
        variables.put("paymentLink", frontendUrl + "/payments");

        sendTemplatedEmail(
                user.getEmail(),
                "URGENT: Rent Payment Overdue - Smart Rental Management",
                "email/overdue-notice",
                variables
        );
    }

    // ===========================================
    // Maintenance Request Emails
    // ===========================================

    /**
     * Send maintenance request confirmation to tenant.
     * @param user the tenant
     * @param requestId request ID
     * @param title request title
     * @param propertyName property name
     */
    @Async
    public void sendMaintenanceRequestConfirmation(User user, String requestId, String title, String propertyName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("requestId", requestId);
        variables.put("title", title);
        variables.put("propertyName", propertyName);

        sendTemplatedEmail(
                user.getEmail(),
                "Maintenance Request Submitted - Smart Rental Management",
                "email/maintenance-confirmation",
                variables
        );
    }

    /**
     * Send maintenance request notification to landlord.
     * @param landlord the landlord
     * @param tenantName tenant name
     * @param requestId request ID
     * @param title request title
     * @param propertyName property name
     * @param urgency urgency level
     */
    @Async
    public void sendMaintenanceNotificationToLandlord(User landlord, String tenantName, String requestId,
                                                       String title, String propertyName, String urgency) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", landlord.getFirstName());
        variables.put("tenantName", tenantName);
        variables.put("requestId", requestId);
        variables.put("title", title);
        variables.put("propertyName", propertyName);
        variables.put("urgency", urgency);

        sendTemplatedEmail(
                landlord.getEmail(),
                "New Maintenance Request - Smart Rental Management",
                "email/maintenance-landlord-notification",
                variables
        );
    }

    /**
     * Send maintenance status update.
     * @param user the tenant
     * @param requestId request ID
     * @param title request title
     * @param status new status
     * @param notes admin notes
     */
    @Async
    public void sendMaintenanceStatusUpdate(User user, String requestId, String title, String status, String notes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("requestId", requestId);
        variables.put("title", title);
        variables.put("status", status);
        variables.put("notes", notes);

        sendTemplatedEmail(
                user.getEmail(),
                "Maintenance Request Update - Smart Rental Management",
                "email/maintenance-status-update",
                variables
        );
    }

    // ===========================================
    // Document Emails
    // ===========================================

    /**
     * Send document upload notification.
     * @param user the recipient
     * @param documentName document name
     * @param documentType document type
     * @param propertyName property name
     * @param downloadLink download link
     */
    @Async
    public void sendDocumentNotification(User user, String documentName, String documentType,
                                          String propertyName, String downloadLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("documentName", documentName);
        variables.put("documentType", documentType);
        variables.put("propertyName", propertyName);
        variables.put("downloadLink", downloadLink);

        sendTemplatedEmail(
                user.getEmail(),
                "New Document Available - Smart Rental Management",
                "email/document-notification",
                variables
        );
    }

    // ===========================================
    // Generic Email Methods
    // ===========================================

    /**
     * Send simple text email.
     * @param to recipient email
     * @param subject email subject
     * @param text email body
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template.
     * @param to recipient email
     * @param subject email subject
     * @param templateName template name (without .html)
     * @param variables template variables
     */
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Templated email sent to: {} using template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send templated email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing email template {}: {}", templateName, e.getMessage(), e);
        }
    }
}