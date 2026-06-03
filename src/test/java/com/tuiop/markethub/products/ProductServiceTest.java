package com.tuiop.markethub.products;

import com.tuiop.markethub.categories.Category;
import com.tuiop.markethub.categories.CategoryRepository;
import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.merchants.MerchantRepository;
import com.tuiop.markethub.products.dto.CreateProductRequest;
import com.tuiop.markethub.products.dto.ProductResponse;
import com.tuiop.markethub.products.images.ProductImageRepository;
import com.tuiop.markethub.products.images.mapper.ProductImageMapper;
import com.tuiop.markethub.products.mapper.ProductMapper;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private CustomUserDetails principal;

    @InjectMocks
    private ProductService productService;

    @Test
    void createMyProduct_whenMerchantIsVerifiedAndActive_savesProductOwnedByPrincipalMerchant() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Merchant merchant = merchant(merchantId, userId, true, true);
        Category category = Category.builder()
                .id(categoryId)
                .name("Electronics")
                .active(true)
                .build();
        CreateProductRequest request = new CreateProductRequest(
                "  Keyboard  ",
                "Mechanical keyboard",
                12_999L,
                7,
                categoryId
        );
        ProductResponse mappedResponse = new ProductResponse(
                UUID.randomUUID(),
                merchantId,
                categoryId,
                "Keyboard",
                "Mechanical keyboard",
                12_999L,
                7,
                true,
                null,
                null,
                List.of()
        );

        given(principal.getUserId()).willReturn(userId);
        given(merchantRepository.findByUserId(userId)).willReturn(Optional.of(merchant));
        given(categoryRepository.findByIdAndActiveTrue(categoryId)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(productMapper.toResponse(any(Product.class), eq(List.of()))).willReturn(mappedResponse);

        ProductResponse response = productService.createMyProduct(principal, request);

        assertThat(response).isSameAs(mappedResponse);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getMerchant()).isSameAs(merchant);
        assertThat(savedProduct.getCategory()).isSameAs(category);
        assertThat(savedProduct.getName()).isEqualTo("Keyboard");
        assertThat(savedProduct.getDescription()).isEqualTo("Mechanical keyboard");
        assertThat(savedProduct.getPriceCents()).isEqualTo(12_999L);
        assertThat(savedProduct.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void createMyProduct_whenMerchantIsNotVerified_throwsAndDoesNotCreateProduct() {
        UUID userId = UUID.randomUUID();
        Merchant merchant = merchant(UUID.randomUUID(), userId, false, true);
        CreateProductRequest request = new CreateProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                12_999L,
                7,
                UUID.randomUUID()
        );

        given(principal.getUserId()).willReturn(userId);
        given(merchantRepository.findByUserId(userId)).willReturn(Optional.of(merchant));

        assertThatThrownBy(() -> productService.createMyProduct(principal, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Merchant must be verified to manage products");

        verifyNoInteractions(categoryRepository, productImageRepository, productImageMapper);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toResponse(any(Product.class), any());
    }

    private Merchant merchant(UUID merchantId, UUID userId, boolean verified, boolean active) {
        return Merchant.builder()
                .id(merchantId)
                .user(User.builder()
                        .id(userId)
                        .firstName("Tymur")
                        .lastName("Kurkov")
                        .birthDate(LocalDate.of(2004, 5, 12))
                        .email("merchant-" + userId + "@example.com")
                        .passwordHash("bcrypt-hash")
                        .role(UserRole.MERCHANT)
                        .enabled(true)
                        .authProvider(AuthProvider.LOCAL)
                        .build())
                .shopName("Tech Shop")
                .description("Computer hardware")
                .verified(verified)
                .active(active)
                .build();
    }
}
