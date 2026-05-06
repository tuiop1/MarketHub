package com.tuiop.markethub.merchants.dto;

import java.time.Instant;
import java.util.UUID;

public record MerchantResponse(
        UUID id,
        UUID userId,
        String shopName,
        String description,
        Boolean verified,
        Boolean active,
        Instant createdAt

){

}
