package com.tuiop.markethub.products;


import com.tuiop.markethub.products.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse> > getProducts(Pageable pageable) {
        return ResponseEntity.ok( productService.getPublicProducts(pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity< ProductResponse> getProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok( productService.getPublicProduct(productId));
    }
}