package com.safeexchange.dto;

public record RazorpayOrderResponse(
        String orderId,
        String keyId,
        Long amount,
        String currency
) {
}