package com.safeexchange.controller;

import com.safeexchange.dto.DepositRequest;
import com.safeexchange.dto.WalletResponse;
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

    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(Authentication authentication) {
        WalletResponse response = walletService.getBalance(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(@Valid @RequestBody DepositRequest request,
                                                  Authentication authentication) {
        WalletResponse response = walletService.deposit(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }
}