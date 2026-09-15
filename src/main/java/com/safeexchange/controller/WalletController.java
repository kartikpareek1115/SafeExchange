package com.safeexchange.controller;

import com.safeexchange.dto.CreateRazorpayOrderRequest;
import com.safeexchange.dto.RazorpayOrderResponse;
import com.safeexchange.dto.VerifyPaymentRequest;
import com.safeexchange.dto.WalletResponse;
import com.safeexchange.service.RazorpayService;
import com.safeexchange.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;
    private final RazorpayService razorpayService;

    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(
            Authentication authentication
    ) {

        WalletResponse response =
                walletService.getBalance(authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @Valid @RequestBody CreateRazorpayOrderRequest request,
            Authentication authentication
    ) {

        RazorpayOrderResponse response =
                razorpayService.createOrder(
                        authentication.getName(),
                        request.amount()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) {

        razorpayService.verifyAndCreditWallet(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok(
                "Payment verified and wallet credited successfully"
        );
    }
}