package com.tuiop.markethub.categories.dto;


import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        Boolean active,
        Instant createdAt
) {
}