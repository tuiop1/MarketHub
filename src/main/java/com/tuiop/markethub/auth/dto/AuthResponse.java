package com.tuiop.markethub.security.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
