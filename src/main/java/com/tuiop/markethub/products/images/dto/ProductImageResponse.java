package com.tuiop.markethub.products.images.dto;

import java.time.Instant;
import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        UUID productId,
        String filename,
        String contentType,
        Long sizeBytes,
        Instant createdAt
) {}