package com.tuiop.markethub.products.images;

import com.tuiop.markethub.products.images.dto.ImageDownloadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-images")
@RequiredArgsConstructor

public class ProductImageController {

    private final ProductImageService productImageService;


    @GetMapping("/{productImageId}/content")
    public ResponseEntity<Resource> downloadImage(
            @PathVariable UUID productImageId

    ) throws IOException {
        ImageDownloadResponse image = productImageService.download(productImageId);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(image.filename(),
                                        StandardCharsets.UTF_8).build().toString())
                .body(image.resource());


    }
}
