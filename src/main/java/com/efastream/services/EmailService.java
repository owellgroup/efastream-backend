package com.efastream.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:info@simplyfound.com.na}")
    private String fromEmail;

    @Value("${app.logo-url:https://www.efastream.com/assets/logo-D4CxNzNq.png}")
    private String logoUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final String EMAIL_HEADER = """
            <!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>
            body{font-family:'Segoe UI',Arial,sans-serif;line-height:1.6;color:#000;margin:0;padding:0;background:#f5f5f5;}
            .wrapper{max-width:600px;margin:0 auto;background:#fff;}
            .logo-wrap{text-align:center;padding:32px 24px 24px;border-bottom:3px solid #c7711f;}
            .logo-wrap img{max-width:200px;height:auto;display:block;margin:0 auto;}
            .box{padding:28px 32px;background:#fff;}
            .box h2{color:#000;font-size:22px;margin:0 0 16px;font-weight:600;}
            .box p{margin:0 0 12px;color:#1a1a1a;}
            .btn{display:inline-block;background:#c7711f;color:#fff!important;padding:14px 28px;text-decoration:none;border-radius:6px;margin:20px 0 8px;font-weight:600;font-size:15px;}
            .footer{font-size:12px;color:#000;margin:0;padding:20px 32px 28px;border-top:1px solid #e0e0e0;text-align:center;}
            </style></head><body><div class="wrapper"><div class="logo-wrap"><img src="%s" alt="EfaStream"/></div>
            """;

    private static final String EMAIL_FOOTER = """
            <div class="footer">© EfaStream. All rights reserved.</div></div></body></html>
            """;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Verify your email</h2><p>Hi " + userName + ",</p>" +
                "<p>Please verify your email by clicking the button below.</p>" +
                "<p><a href='" + verifyUrl + "' class='btn'>Verify Email</a></p>" +
                "<p>Or copy this link: " + verifyUrl + "</p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Verify your EfaStream account", html);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Welcome to EfaStream!</h2><p>Hi " + userName + ",</p>" +
                "<p>Your account has been verified. You can now log in and subscribe to enjoy our content.</p>" +
                "<p><a href='" + frontendUrl + "/login' class='btn'>Log In</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Welcome to EfaStream", html);
    }

    @Async
    public void sendSubscriptionSuccess(String toEmail, String userName, String planName, String endDate) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Subscription activated</h2><p>Hi " + userName + ",</p>" +
                "<p>Your subscription to <strong>" + planName + "</strong> is now active. It is valid until " + endDate + ".</p>" +
                "<p><a href='" + frontendUrl + "/browse' class='btn'>Start watching</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Subscription activated - EfaStream", html);
    }

    @Async
    public void sendSubscriptionExpiringSoon(String toEmail, String userName, String planName, String endDate) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Subscription expiring soon</h2><p>Hi " + userName + ",</p>" +
                "<p>Your subscription to <strong>" + planName + "</strong> will expire on " + endDate + ".</p>" +
                "<p><a href='" + frontendUrl + "/subscription' class='btn'>Renew now</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Subscription expiring soon - EfaStream", html);
    }

    @Async
    public void sendSubscriptionExpired(String toEmail, String userName) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Subscription expired</h2><p>Hi " + userName + ",</p>" +
                "<p>Your subscription has expired. Renew to continue watching.</p>" +
                "<p><a href='" + frontendUrl + "/subscription' class='btn'>Renew</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Subscription expired - EfaStream", html);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Reset your password</h2><p>Hi " + userName + ",</p>" +
                "<p>You requested a password reset. Click the button below to set a new password.</p>" +
                "<p><a href='" + resetUrl + "' class='btn'>Reset Password</a></p>" +
                "<p>This link expires in 1 hour. If you didn't request this, ignore this email.</p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Reset your password - EfaStream", html);
    }

    @Async
    public void sendContentApproved(String toEmail, String partnerName, String contentTitle) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Content approved</h2><p>Hi " + partnerName + ",</p>" +
                "<p>Your content <strong>\"" + contentTitle + "\"</strong> has been approved and is now visible on the platform.</p>" +
                "<p><a href='" + frontendUrl + "/partner/content' class='btn'>View content</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Content approved - EfaStream", html);
    }

    @Async
    public void sendContentRejected(String toEmail, String partnerName, String contentTitle, String reason) {
        String html = String.format(EMAIL_HEADER, logoUrl) +
                "<div class='box'><h2>Content rejected</h2><p>Hi " + partnerName + ",</p>" +
                "<p>Your content <strong>\"" + contentTitle + "\"</strong> has been rejected." +
                (reason != null && !reason.isBlank() ? " Reason: " + reason + "." : "") + "</p>" +
                "<p><a href='" + frontendUrl + "/partner/content' class='btn'>View content</a></p></div>" + EMAIL_FOOTER;
        sendHtmlEmail(toEmail, "Content rejected - EfaStream", html);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "EfaStream");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Email sent to {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
