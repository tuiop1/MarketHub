package com.tuiop.markethub.carts.cart;

import com.tuiop.markethub.carts.dto.AddToCartRequest;
import com.tuiop.markethub.carts.dto.CartItemResponse;
import com.tuiop.markethub.carts.dto.CartResponse;
import com.tuiop.markethub.orders.OrderService;
import com.tuiop.markethub.orders.dto.OrderResponse;
import com.tuiop.markethub.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/cart")
@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;


    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(cartService.getMyCart(principal));
    }


    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AddToCartRequest addToCartRequest
            ) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.addProductToMyCart(addToCartRequest, principal));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItemFromCart(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id
            ) {
        cartService.removeCartItemFromMyCart(id, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<OrderResponse> purchaseMyCart(@AuthenticationPrincipal CustomUserDetails principal){
        return  ResponseEntity.status(HttpStatus.CREATED).body(orderService.purchaseMyCart(principal));
    }


}
