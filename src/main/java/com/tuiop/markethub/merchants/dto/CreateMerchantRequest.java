package com.tuiop.markethub.merchants.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMerchantRequest(
        @NotBlank
        @Size(max = 255)
        String shopName,

        @Size(max = 1000)
        String description
) {
}
