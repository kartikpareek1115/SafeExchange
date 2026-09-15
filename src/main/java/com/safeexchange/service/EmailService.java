package com.safeexchange.service;

import com.safeexchange.entity.OtpPurpose;
import com.safeexchange.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode, OtpPurpose purpose) {
        String subject = purpose == OtpPurpose.REGISTRATION
                ? "Verify your SafeExchange account"
                : "Reset your SafeExchange password";

        String heading = purpose == OtpPurpose.REGISTRATION
                ? "Confirm your email address"
                : "Reset your password";

        String body = purpose == OtpPurpose.REGISTRATION
                ? "Use the code below to verify your email and activate your SafeExchange account."
                : "Use the code below to reset your SafeExchange password. If you didn't request this, you can ignore this email.";

        String html = """
                <div style="font-family:Arial,sans-serif;background:#0b0f14;padding:32px;color:#e6edf3">
                  <div style="max-width:480px;margin:0 auto;background:#111820;border-radius:12px;padding:32px;border:1px solid #1f2a35">
                    <h2 style="color:#10b981;margin-top:0">SafeExchange</h2>
                    <h3 style="margin-bottom:4px">%s</h3>
                    <p style="color:#9aa8b4;font-size:14px">%s</p>
                    <div style="background:#0b0f14;border:1px solid #1f2a35;border-radius:8px;padding:16px;text-align:center;margin:20px 0">
                      <span style="font-size:28px;font-weight:700;letter-spacing:6px;color:#10b981">%s</span>
                    </div>
                    <p style="color:#6b7885;font-size:12px">This code expires in 10 minutes. Do not share it with anyone.</p>
                  </div>
                </div>
                """.formatted(heading, body, otpCode);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send OTP email: " + e.getMessage());
        }
    }
}