package com.tuiop.markethub.merchants;

import com.tuiop.markethub.merchants.dto.CreateMerchantRequest;
import com.tuiop.markethub.merchants.dto.MerchantResponse;
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

@RequestMapping("/api/v1/merchants")
@RestController
@RequiredArgsConstructor

public class MerchantController {


    private final MerchantService merchantService;


    @GetMapping
    public ResponseEntity<Page<MerchantResponse>> getAllActiveAndVerifiedMerchants(Pageable pageable) {
        return ResponseEntity.ok(merchantService.getAllActiveAndVerifiedMerchants(pageable));
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me")
    public ResponseEntity<MerchantResponse> createMerchant(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.createMerchant(principal, request));

    }


    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/me")
    public ResponseEntity<MerchantResponse> getMyMerchant(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(merchantService.getMyMerchant(principal));

    }

}
