package com.safeexchange.controller;

import com.safeexchange.dto.CreateEscrowRequest;
import com.safeexchange.dto.EscrowResponse;
import com.safeexchange.dto.SubmitAssetRequest;
import com.safeexchange.service.EscrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EscrowController {

    private final EscrowService escrowService;

    @PostMapping
    public ResponseEntity<EscrowResponse> createEscrow(@Valid @RequestBody CreateEscrowRequest request,
                                                       Authentication authentication) {
        EscrowResponse response = escrowService.createEscrow(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/lock-funds")
    public ResponseEntity<EscrowResponse> lockFunds(@PathVariable Long id, Authentication authentication) {
        EscrowResponse response = escrowService.lockFunds(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit-asset")
    public ResponseEntity<EscrowResponse> submitAsset(@PathVariable Long id,
                                                      @Valid @RequestBody SubmitAssetRequest request,
                                                      Authentication authentication) {
        EscrowResponse response = escrowService.submitAsset(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<EscrowResponse> confirmDelivery(@PathVariable Long id, Authentication authentication) {
        EscrowResponse response = escrowService.confirmDelivery(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}