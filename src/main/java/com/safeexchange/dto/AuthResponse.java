package com.safeexchange.dto;

public record AuthResponse(
        String token,
        String type,
        String username
) {
    public AuthResponse(String token, String username) {
        this(token, "Bearer", username);
    }
}