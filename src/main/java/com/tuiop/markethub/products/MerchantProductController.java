package com.tuiop.markethub.products;


import com.tuiop.markethub.products.dto.CreateProductRequest;
import com.tuiop.markethub.products.dto.ProductResponse;
import com.tuiop.markethub.products.dto.UpdateProductRequest;
import com.tuiop.markethub.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createMyProduct(principal, request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        return ResponseEntity.ok( productService.getMyProducts(principal, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getMyProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok( productService.getMyProduct(principal, productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateMyProduct(principal, productId, request));
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID productId
    ) {
        productService.deleteMyProduct(principal, productId);
    }
}