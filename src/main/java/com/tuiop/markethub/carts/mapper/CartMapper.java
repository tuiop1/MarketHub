package com.tuiop.markethub.carts.mapper;

import com.tuiop.markethub.carts.cart.Cart;
import com.tuiop.markethub.carts.cart.item.CartItem;
import com.tuiop.markethub.carts.dto.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cartItems", source = "cartItems")
    @Mapping(target = "totalPriceCents", expression = "java(calculateTotalPriceCents(cart))")
    CartResponse toCartResponse(Cart cart);

    default Long calculateTotalPriceCents(Cart cart){
        if (cart == null || cart.getCartItems() == null) {
            return  0L;
        }
        return cart.getCartItems().stream().mapToLong(this::calculateTotalLinePriceCents).sum();

    }

    default Long calculateTotalLinePriceCents(CartItem cartItem){
        if(cartItem == null || cartItem.getProduct() == null){
            return  0L;
        }
        Integer quantity = cartItem.getQuantity();
        Long productPrice = cartItem.getProduct().getPriceCents();

        if(quantity == null || productPrice == null){
            return 0L;
        }

        return productPrice * quantity;

    }
}
