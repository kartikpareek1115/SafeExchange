package com.safeexchange.service;

import com.safeexchange.entity.OtpPurpose;
import com.safeexchange.entity.OtpVerification;
import com.safeexchange.exception.InvalidOtpException;
import com.safeexchange.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        String code = generateSixDigitCode();

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpCode(code)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();

        otpRepository.save(otp);
        emailService.sendOtpEmail(email, code, purpose);
    }

    @Transactional
    public void verifyOtp(String email, String code, OtpPurpose purpose) {
        OtpVerification otp = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new InvalidOtpException(
                        "No pending OTP found for this email. Please request a new code."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("This OTP has expired. Please request a new code.");
        }

        if (!otp.getOtpCode().equals(code)) {
            throw new InvalidOtpException("Incorrect OTP code.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private String generateSixDigitCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}