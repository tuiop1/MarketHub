package com.tuiop.markethub.carts.cart;

import com.tuiop.markethub.carts.cart.item.CartItem;
import com.tuiop.markethub.carts.cart.item.CartItemRepository;
import com.tuiop.markethub.carts.dto.AddToCartRequest;
import com.tuiop.markethub.carts.dto.CartItemResponse;
import com.tuiop.markethub.carts.mapper.CartItemMapper;
import com.tuiop.markethub.carts.mapper.CartMapper;
import com.tuiop.markethub.categories.Category;
import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.ProductRepository;
import com.tuiop.markethub.products.exceptions.InsufficientStockException;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CustomUserDetails principal;



    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;


    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_whenExistingItemFinalQuantityFitsStock_increasesExistingItemAndDoesNotCreateNewItem() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Product product = activeBuyableProduct(productId, 10);
        Cart cart = Cart.builder().id(cartId).build();
        CartItem existingItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        given(principal.getUserId()).willReturn(userId);
        given(productRepository.findWithMerchantAndCategoryById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findDetailedByUserIdForUpdate(userId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartIdAndProductIdForUpdate(cartId, productId)).willReturn(Optional.of(existingItem));
        given(cartItemMapper.toCartItemResponse(existingItem)).willReturn(cartItemResponse(cartId, productId, 4));

        CartItemResponse response = cartService.addProductToMyCart(new AddToCartRequest(productId, 2), principal);

        assertThat(response.quantity()).isEqualTo(4);
        assertThat(existingItem.getQuantity()).isEqualTo(4);
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartItemMapper).toCartItemResponse(existingItem);
    }

    @Test
    void addToCart_whenExistingItemFinalQuantityExceedsStock_throwsAndDoesNotMutateExistingItem() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Product product = activeBuyableProduct(productId, 10);
        Cart cart = Cart.builder().id(cartId).build();
        CartItem existingItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(9)
                .build();

        given(principal.getUserId()).willReturn(userId);
        given(productRepository.findWithMerchantAndCategoryById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findDetailedByUserIdForUpdate(userId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartIdAndProductIdForUpdate(cartId, productId)).willReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> cartService.addProductToMyCart(new AddToCartRequest(productId, 2), principal))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Requested: 11")
                .hasMessageContaining("available: 10");

        assertThat(existingItem.getQuantity()).isEqualTo(9);
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verifyNoInteractions(cartItemMapper);
    }

    @Test
    void addToCart_whenRequestedQuantityExceedsStockForNewItem_throwsBeforeLoadingCart() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = activeBuyableProduct(productId, 10);

        given(principal.getUserId()).willReturn(userId);
        given(productRepository.findWithMerchantAndCategoryById(productId)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addProductToMyCart(new AddToCartRequest(productId, 11), principal))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Requested: 11")
                .hasMessageContaining("available: 10");

        verifyNoInteractions(cartRepository, cartItemRepository, cartItemMapper);
    }

    @Test
    void addToCart_whenProductIsNotInCart_savesNewCartItemWithRequestedQuantity() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Product product = activeBuyableProduct(productId, 10);
        Cart cart = Cart.builder().id(cartId).build();

        given(principal.getUserId()).willReturn(userId);
        given(productRepository.findWithMerchantAndCategoryById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findDetailedByUserIdForUpdate(userId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartIdAndProductIdForUpdate(cartId, productId)).willReturn(Optional.empty());
        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(cartItemMapper.toCartItemResponse(any(CartItem.class))).willReturn(cartItemResponse(cartId, productId, 3));

        CartItemResponse response = cartService.addProductToMyCart(new AddToCartRequest(productId, 3), principal);

        assertThat(response.quantity()).isEqualTo(3);

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemCaptor.capture());

        CartItem savedItem = cartItemCaptor.getValue();
        assertThat(savedItem.getCart()).isSameAs(cart);
        assertThat(savedItem.getProduct()).isSameAs(product);
        assertThat(savedItem.getQuantity()).isEqualTo(3);
    }

    private Product activeBuyableProduct(UUID productId, int stockQuantity) {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Keyboards")
                .active(true)
                .build();

        Merchant merchant = Merchant.builder()
                .id(UUID.randomUUID())
                .shopName("Tech Shop")
                .active(true)
                .verified(true)
                .build();

        return Product.builder()
                .id(productId)
                .merchant(merchant)
                .category(category)
                .name("Keyboard")
                .priceCents(5_000L)
                .stockQuantity(stockQuantity)
                .active(true)
                .build();
    }

    private CartItemResponse cartItemResponse(UUID cartId, UUID productId, int quantity) {
        return new CartItemResponse(
                UUID.randomUUID(),
                cartId,
                productId,
                "Keyboard",
                5_000L,
                quantity,
                5_000L * quantity,
                true,
                10
        );
    }
}
