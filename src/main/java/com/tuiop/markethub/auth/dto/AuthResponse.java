package com.tuiop.markethub.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
