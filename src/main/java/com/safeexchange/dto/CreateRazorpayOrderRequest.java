package com.safeexchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRazorpayOrderRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Minimum deposit is ₹1")
        BigDecimal amount

) {
}