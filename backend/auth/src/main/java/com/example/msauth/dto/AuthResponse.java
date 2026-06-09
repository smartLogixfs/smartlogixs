package com.example.msauth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
