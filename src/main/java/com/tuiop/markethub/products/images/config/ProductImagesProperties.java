package com.tuiop.markethub.products.images.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app.product.images")
@Validated
public record ProductImagesProperties(
        @Min(1)
        Integer maxFileSizeBytes,
        @Min(1)
        Integer maxImagesPerProduct,
        @NotEmpty
        List<String> allowedContentTypes
) {}