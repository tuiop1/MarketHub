package com.tuiop.markethub.orders;

import com.tuiop.markethub.carts.cart.CartRepository;
import com.tuiop.markethub.categories.Category;
import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.orders.dto.OrderResponse;
import com.tuiop.markethub.orders.dto.PurchaseItemRequest;
import com.tuiop.markethub.orders.dto.PurchaseRequest;
import com.tuiop.markethub.orders.enums.OrderStatus;
import com.tuiop.markethub.orders.enums.PaymentStatus;
import com.tuiop.markethub.orders.mapper.OrderMapper;
import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.ProductRepository;
import com.tuiop.markethub.products.exceptions.InsufficientStockException;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import com.tuiop.markethub.users.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;



    @Mock
    private CustomUserDetails principal;

    @InjectMocks
    private OrderService orderService;

    @Test
    void purchase_whenRequestContainsDuplicateProducts_mergesQuantitiesLocksProductAndSavesSingleOrderItem() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        User buyer = user(buyerId, UserRole.USER, "buyer@example.com");
        Product product = product(productId, merchantOwnedBy(UUID.randomUUID()), 10, 1_500L);
        PurchaseRequest request = new PurchaseRequest(List.of(
                new PurchaseItemRequest(productId, 2),
                new PurchaseItemRequest(productId, 3)
        ));
        OrderResponse mappedResponse = new OrderResponse(
                UUID.randomUUID(),
                OrderStatus.CREATED,
                7_500L,
                List.of(),
                null,
                PaymentStatus.PENDING
        );

        given(principal.getUserId()).willReturn(buyerId);
        given(userRepository.findById(buyerId)).willReturn(Optional.of(buyer));
        given(productRepository.findBuyableByIdsForUpdate(anyCollection())).willReturn(List.of(product));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toOrderResponse(any(Order.class))).willReturn(mappedResponse);

        OrderResponse response = orderService.purchase(principal, request);

        assertThat(response).isSameAs(mappedResponse);
        assertThat(product.getStockQuantity()).isEqualTo(5);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<UUID>> lockedProductIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(productRepository).findBuyableByIdsForUpdate(lockedProductIdsCaptor.capture());
        assertThat(lockedProductIdsCaptor.getValue()).containsExactly(productId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUser()).isSameAs(buyer);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedOrder.getTotalPriceCents()).isEqualTo(7_500L);
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        OrderItem savedItem = savedOrder.getOrderItems().getFirst();
        assertThat(savedItem.getOrder()).isSameAs(savedOrder);
        assertThat(savedItem.getProduct()).isSameAs(product);
        assertThat(savedItem.getMerchant()).isSameAs(product.getMerchant());
        assertThat(savedItem.getProductNameSnapshot()).isEqualTo("Keyboard");
        assertThat(savedItem.getMerchantNameSnapshot()).isEqualTo("Tech Shop");
        assertThat(savedItem.getPriceSnapshotCents()).isEqualTo(1_500L);
        assertThat(savedItem.getQuantity()).isEqualTo(5);
        assertThat(savedItem.getTotalPriceSnapshotCents()).isEqualTo(7_500L);
    }

    @Test
    void purchase_whenBuyerOwnsProduct_throwsAndDoesNotDecreaseStockOrSaveOrder() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        User buyer = user(buyerId, UserRole.MERCHANT, "merchant@example.com");
        Product product = product(productId, merchantOwnedBy(buyer), 10, 1_500L);
        PurchaseRequest request = new PurchaseRequest(List.of(new PurchaseItemRequest(productId, 1)));

        given(principal.getUserId()).willReturn(buyerId);
        given(userRepository.findById(buyerId)).willReturn(Optional.of(buyer));
        given(productRepository.findBuyableByIdsForUpdate(anyCollection())).willReturn(List.of(product));

        assertThatThrownBy(() -> orderService.purchase(principal, request))
                .isInstanceOf(OwnProductPurchaseException.class)
                .hasMessageContaining("cannot purchase your own product");

        assertThat(product.getStockQuantity()).isEqualTo(10);
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderMapper, never()).toOrderResponse(any(Order.class));
    }

    @Test
    void purchase_whenRequestedQuantityExceedsStock_throwsAndDoesNotSaveOrder() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        User buyer = user(buyerId, UserRole.USER, "buyer@example.com");
        Product product = product(productId, merchantOwnedBy(UUID.randomUUID()), 2, 1_500L);
        PurchaseRequest request = new PurchaseRequest(List.of(new PurchaseItemRequest(productId, 3)));

        given(principal.getUserId()).willReturn(buyerId);
        given(userRepository.findById(buyerId)).willReturn(Optional.of(buyer));
        given(productRepository.findBuyableByIdsForUpdate(anyCollection())).willReturn(List.of(product));

        assertThatThrownBy(() -> orderService.purchase(principal, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Requested: 3")
                .hasMessageContaining("available: 2");

        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderMapper, never()).toOrderResponse(any(Order.class));
    }

    private Product product(UUID productId, Merchant merchant, int stockQuantity, long priceCents) {
        return Product.builder()
                .id(productId)
                .merchant(merchant)
                .category(Category.builder()
                        .id(UUID.randomUUID())
                        .name("Electronics")
                        .active(true)
                        .build())
                .name("Keyboard")
                .description("Mechanical keyboard")
                .priceCents(priceCents)
                .stockQuantity(stockQuantity)
                .active(true)
                .build();
    }

    private Merchant merchantOwnedBy(UUID ownerId) {
        return merchantOwnedBy(user(ownerId, UserRole.MERCHANT, "merchant-" + ownerId + "@example.com"));
    }

    private Merchant merchantOwnedBy(User owner) {
        return Merchant.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .shopName("Tech Shop")
                .description("Computer hardware")
                .verified(true)
                .active(true)
                .build();
    }

    private User user(UUID userId, UserRole role, String email) {
        return User.builder()
                .id(userId)
                .firstName("Tymur")
                .lastName("Kurkov")
                .birthDate(LocalDate.of(2004, 5, 12))
                .email(email)
                .passwordHash("bcrypt-hash")
                .role(role)
                .enabled(true)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
}
