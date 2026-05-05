package com.tuiop.markethub.security.auth.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String password
) {
}
