package com.tuiop.markethub.products.images.mapper;

import com.tuiop.markethub.products.images.ProductImage;
import com.tuiop.markethub.products.images.dto.ImageDownloadResponse;
import com.tuiop.markethub.products.images.dto.ProductImageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {


    @Mapping(target = "filename", source = "objectKey")
    @Mapping(target = "productId", source = "product.id")
    ProductImageResponse toProductImageResponse(ProductImage image);
}
