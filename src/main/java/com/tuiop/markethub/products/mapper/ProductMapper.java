package com.tuiop.markethub.products.mapper;

import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.dto.ProductResponse;
import com.tuiop.markethub.products.images.dto.ProductImageResponse;
import com.tuiop.markethub.products.images.mapper.ProductImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProductImageMapper.class)
public interface ProductMapper {

    @Mapping(target = "merchantId", source = "merchant.id")
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);

    @Mapping(target = "merchantId", source = "merchant.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "images", ignore = true)
    ProductResponse toResponseWithoutImages(Product product);

    default ProductResponse toResponse(Product product, List<ProductImageResponse> images) {
        ProductResponse response = toResponseWithoutImages(product);

        return new ProductResponse(
                response.id(),
                response.merchantId(),
                response.categoryId(),
                response.name(),
                response.description(),
                response.priceCents(),
                response.stockQuantity(),
                response.active(),
                response.createdAt(),
                response.updatedAt(),
                images
        );
    }
}
