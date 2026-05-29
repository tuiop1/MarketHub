package com.tuiop.markethub.products.images.dto;

import org.springframework.core.io.Resource;

public record ImageDownloadResponse(
        Resource resource,
        String contentType,
        String filename
) {
}