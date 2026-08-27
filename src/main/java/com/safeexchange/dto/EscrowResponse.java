package com.safeexchange.dto;

import com.safeexchange.entity.EscrowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EscrowResponse(
        Long id,
        String buyerUsername,
        String sellerUsername,
        String assetName,
        String assetType,
        String assetDescription,
        String submissionLink,
        BigDecimal amount,
        EscrowStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}