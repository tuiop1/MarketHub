package com.tuiop.markethub.products.mapper;

import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "merchantId", source = "merchant.id")
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
