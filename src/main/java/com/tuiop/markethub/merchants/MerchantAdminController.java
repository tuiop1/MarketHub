package com.tuiop.markethub.merchants;

import com.tuiop.markethub.merchants.dto.MerchantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MerchantAdminController {

    private final MerchantService merchantService;



    @GetMapping("/unverified")
    public Page<MerchantResponse> getUnverifiedMerchants(Pageable pageable) {
        return merchantService.getUnverifiedMerchants(pageable);
    }

    @PatchMapping("/{merchantId}/verify")
    public MerchantResponse verifyMerchant(@PathVariable UUID merchantId) {
        return merchantService.verifyMerchant(merchantId);
    }

    @PatchMapping("/{merchantId}/disable")
    public MerchantResponse disableMerchant(@PathVariable UUID merchantId) {
        return merchantService.disableMerchant(merchantId);
    }

    @PatchMapping("/{merchantId}/enable")
    public MerchantResponse enableMerchant(@PathVariable UUID merchantId) {
        return merchantService.enableMerchant(merchantId);
    }


}
