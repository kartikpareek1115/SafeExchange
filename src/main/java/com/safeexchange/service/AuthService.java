package com.safeexchange.service;

import com.safeexchange.dto.*;
import com.safeexchange.entity.OtpPurpose;
import com.safeexchange.entity.User;
import com.safeexchange.entity.Wallet;
import com.safeexchange.exception.DuplicateResourceException;
import com.safeexchange.exception.UserNotFoundException;
import com.safeexchange.repository.UserRepository;
import com.safeexchange.repository.WalletRepository;
import com.safeexchange.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        otpService.generateAndSendOtp(savedUser.getEmail(), OtpPurpose.REGISTRATION);

        return new MessageResponse("Account created. A 6-digit verification code has been sent to " + savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // If the account's email is not verified, UserDetailsServiceImpl marks it disabled,
        // and Spring Security's DaoAuthenticationProvider throws DisabledException here
        // before password checking even happens on a fresh session — see GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtUtil.generateToken(request.username());
        return new AuthResponse(token, request.username());
    }

    @Transactional
    public MessageResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTRATION);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.email()));
        user.setEmailVerified(true);
        userRepository.save(user);

        return new MessageResponse("Email verified successfully. You can now sign in.");
    }

    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.email()));

        if (user.isEmailVerified()) {
            throw new DuplicateResourceException("This email is already verified.");
        }

        otpService.generateAndSendOtp(user.getEmail(), OtpPurpose.REGISTRATION);
        return new MessageResponse("A new verification code has been sent to " + user.getEmail());
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // Deliberately return the same message whether or not the email exists,
        // so this endpoint can't be used to enumerate registered accounts.
        userRepository.findByEmail(request.email())
                .ifPresent(user -> otpService.generateAndSendOtp(user.getEmail(), OtpPurpose.PASSWORD_RESET));

        return new MessageResponse("If that email is registered, a reset code has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.PASSWORD_RESET);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.email()));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new MessageResponse("Password reset successfully. You can now sign in with your new password.");
    }
}