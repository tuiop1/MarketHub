package com.tuiop.markethub.orders;

import com.tuiop.markethub.orders.dto.OrderResponse;
import com.tuiop.markethub.orders.dto.PurchaseRequest;
import com.tuiop.markethub.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse purchase(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PurchaseRequest request
    ) {
        return orderService.purchase(principal, request);
    }

    @GetMapping("/me")
    public Page<OrderResponse> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        return orderService.getMyOrders(principal, pageable);
    }
}