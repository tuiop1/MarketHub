package com.tuiop.markethub.products;


import com.tuiop.markethub.categories.Category;
import com.tuiop.markethub.categories.CategoryRepository;
import com.tuiop.markethub.common.exceptions.ResourceNotFoundException;
import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.merchants.MerchantRepository;
import com.tuiop.markethub.products.dto.CreateProductRequest;
import com.tuiop.markethub.products.dto.ProductResponse;
import com.tuiop.markethub.products.dto.UpdateProductRequest;
import com.tuiop.markethub.products.mapper.ProductMapper;
import com.tuiop.markethub.security.user.CustomUserDetails;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @PreAuthorize("hasRole('MERCHANT')")
    @Transactional


    public ProductResponse createMyProduct(CustomUserDetails principal, CreateProductRequest request) {
        Merchant merchant = getMerchantByUserId(principal.getUserId());

        validateMerchantCanManageProducts(merchant);

        Category category = getCategory(request.categoryId());

        Product product = Product.builder()
                .merchant(merchant)
                .category(category)
                .name(request.name().trim())
                .description(request.description())
                .priceCents(request.priceCents())
                .stockQuantity(request.stockQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info(
                "Product created: productId={}, merchantId={}, categoryId={}, name={}, priceCents={}, stockQuantity={}",
                savedProduct.getId(),
                merchant.getId(),
                category.getId(),
                savedProduct.getName(),
                savedProduct.getPriceCents(),
                savedProduct.getStockQuantity()
        );
        return productMapper.toResponse(savedProduct);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyProducts(CustomUserDetails principal, Pageable pageable) {
        Merchant merchant = getMerchantByUserId(principal.getUserId());

        return productRepository.findByMerchantId(merchant.getId(), pageable)
                .map(productMapper::toResponse);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @Transactional(readOnly = true)
    public ProductResponse getMyProduct(CustomUserDetails principal, UUID productId) {
        Merchant merchant = getMerchantByUserId(principal.getUserId());

        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));

        return productMapper.toResponse(product);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @Transactional
    @CacheEvict(value = "public-product", key = "#productId")
    public ProductResponse updateMyProduct(
            CustomUserDetails principal,
            UUID productId,
            UpdateProductRequest request
    ) {
        Merchant merchant = getMerchantByUserId(principal.getUserId());

        validateMerchantCanManageProducts(merchant);

        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));

        Category category = getCategory(request.categoryId());

        product.update(
                request.name().trim(),
                request.description(),
                request.priceCents(),
                request.stockQuantity(),
                category,
                request.active()
        );

        log.info(
                "Product updated: productId={}, merchantId={}, categoryId={}, name={}, priceCents={}, stockQuantity={}, active={}",
                product.getId(),
                merchant.getId(),
                category.getId(),
                product.getName(),
                product.getPriceCents(),
                product.getStockQuantity(),
                product.getActive()
        );
        return productMapper.toResponse(product);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @Transactional
    @CacheEvict(value = "public-product", key = "#productId")
    public void deleteMyProduct(CustomUserDetails principal, UUID productId) {
        Merchant merchant = getMerchantByUserId(principal.getUserId());

        Product product = productRepository.findByIdAndMerchantId(productId, merchant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));

        product.deactivate();
        log.warn(
                "Product deactivated: productId={}, merchantId={}, name={}",
                product.getId(),
                merchant.getId(),
                product.getName()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getPublicProducts(Pageable pageable) {
        return productRepository.findByActiveTrueAndMerchantActiveTrueAndMerchantVerifiedTrueAndCategoryActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "public-product", key = "#productId")
    public ProductResponse getPublicProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .filter(Product::getActive)
                .filter(p -> p.getMerchant().getActive())
                .filter(p -> p.getMerchant().getVerified())
                .filter(p -> p.getCategory().getActive())
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));

        return productMapper.toResponse(product);
    }

    private Merchant getMerchantByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Merchant.class, "userId", userId));
    }

    private Category getCategory(UUID categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, categoryId));
    }

    private void validateMerchantCanManageProducts(Merchant merchant) {
        if (!merchant.getActive()) {
            throw new IllegalStateException("Merchant is disabled");
        }

        if (!merchant.getVerified()) {
            throw new IllegalStateException("Merchant must be verified to manage products");
        }
    }
}