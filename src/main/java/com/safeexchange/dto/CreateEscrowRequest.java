package com.safeexchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateEscrowRequest(

        @NotBlank(message = "Asset name is required")
        String assetName,

        @NotBlank(message = "Asset type is required")
        String assetType,

        String assetDescription,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount
) {}