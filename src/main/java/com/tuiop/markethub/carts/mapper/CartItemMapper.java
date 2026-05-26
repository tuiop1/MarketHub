package com.tuiop.markethub.carts.mapper;

import com.tuiop.markethub.carts.cart.item.CartItem;
import com.tuiop.markethub.carts.dto.CartItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {


    @Mapping(target = "cartId", source = "cart.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "priceCents", source = "product.priceCents")
    @Mapping(target = "productActive", source = "product.active")
    @Mapping(target = "stockQuantity", source = "product.stockQuantity")
    @Mapping(target = "totalPriceCents", expression = "java(calculateTotalPriceCents(cartItem))")

    CartItemResponse toCartItemResponse(CartItem cartItem);


    default Long calculateTotalPriceCents(CartItem cartItem){

        if(cartItem == null || cartItem.getProduct() == null){
            return 0L;
        }

        Long priceCents = cartItem.getProduct().getPriceCents();
        Integer quantity = cartItem.getQuantity();

        if(priceCents == null || quantity == null) {
            return 0L;
        }
        return priceCents * quantity;
    }
}
