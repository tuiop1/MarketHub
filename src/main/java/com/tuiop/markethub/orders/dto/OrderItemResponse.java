package com.tuiop.markethub.orders.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        UUID merchantId,
        String productNameSnapshot,
        String merchantNameSnapshot,
        Long priceSnapshotCents,
        Integer quantity,
        Long totalPriceSnapshotCents
) {
}

