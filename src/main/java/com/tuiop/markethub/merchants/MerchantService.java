package com.tuiop.markethub.merchants;

import com.tuiop.markethub.common.exceptions.ResourceNotFoundException;
import com.tuiop.markethub.merchants.dto.CreateMerchantRequest;
import com.tuiop.markethub.merchants.dto.MerchantResponse;
import com.tuiop.markethub.merchants.exceptions.MerchantAlreadyExistsException;
import com.tuiop.markethub.merchants.exceptions.ShopNameAlreadyTakenException;
import com.tuiop.markethub.merchants.mapper.MerchantMapper;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final MerchantMapper merchantMapper;

    //merchant will be created unverified, should be verified by admin to be in public access
    @Transactional
    public MerchantResponse createMerchant(CustomUserDetails principal, CreateMerchantRequest request) {


        User authenticatedUser = userRepository.findById(principal.getUserId()).orElseThrow(() -> new ResourceNotFoundException(User.class, principal.getUserId()));

        String shopName = request.shopName().trim();

        if (merchantRepository.existsByShopNameIgnoreCase(shopName)) {
            throw new ShopNameAlreadyTakenException(request.shopName());
        }

        if (merchantRepository.existsByUserId(authenticatedUser.getId())) {
            throw new MerchantAlreadyExistsException();
        }

        Merchant merchant = Merchant.builder()
                .user(authenticatedUser)
                .shopName(shopName)
                .description(request.description())
                .build();


       Merchant savedMerchant =  merchantRepository.save(merchant);
        log.info(
                "Merchant profile created: merchantId={}, userId={}, shopName={}",
                savedMerchant.getId(),
                authenticatedUser.getId(),
                savedMerchant.getShopName()
        );

        // the user will get the role of merchant
        authenticatedUser.makeMerchant();


       return merchantMapper.toResponse(savedMerchant);

    }




    @Transactional(readOnly = true)
    public MerchantResponse getMyMerchant(CustomUserDetails principal){
       Merchant merchant  = merchantRepository.findByUserId(principal.getUserId()).orElseThrow(() ->
               new ResourceNotFoundException(Merchant.class, "userId", principal.getUserId()));

       return merchantMapper.toResponse(merchant);
    }

    @Transactional(readOnly = true)
    public Page<MerchantResponse> getAllActiveAndVerifiedMerchants(Pageable pageable){


       return merchantRepository.findByActiveTrueAndVerifiedTrue(pageable).map(merchantMapper::toResponse);
    }



    //========ADMIN=======


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MerchantResponse verifyMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(Merchant.class, merchantId));

        merchant.verify();
        log.info(
                "Merchant verified by admin: merchantId={}, shopName={}",
                merchant.getId(),
                merchant.getShopName()
        );
        return merchantMapper.toResponse(merchant);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = "public-product", allEntries = true)
    public MerchantResponse disableMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(Merchant.class, merchantId));

        merchant.disable();
        log.warn(
                "Merchant disabled by admin: merchantId={}, shopName={}",
                merchant.getId(),
                merchant.getShopName()
        );
        return merchantMapper.toResponse(merchant);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MerchantResponse enableMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(Merchant.class, merchantId));

        merchant.enable();
        log.info(
                "Merchant enabled by admin: merchantId={}, shopName={}",
                merchant.getId(),
                merchant.getShopName()
        );
        return merchantMapper.toResponse(merchant);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<MerchantResponse> getUnverifiedMerchants(Pageable pageable) {
        return merchantRepository.findByActiveTrueAndVerifiedFalse(pageable)
                .map(merchantMapper::toResponse);
    }

}
