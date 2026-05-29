package com.tuiop.markethub.products.images;


import com.tuiop.markethub.common.exceptions.ResourceNotFoundException;
import com.tuiop.markethub.common.storage.object.ObjectStorageService;
import com.tuiop.markethub.common.storage.object.exceptions.EmptyFileException;
import com.tuiop.markethub.common.storage.object.exceptions.FileTooLargeException;
import com.tuiop.markethub.common.storage.object.exceptions.StorageException;
import com.tuiop.markethub.common.storage.object.exceptions.UnsupportedContentTypeException;
import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.ProductRepository;
import com.tuiop.markethub.products.images.config.ProductImagesProperties;
import com.tuiop.markethub.products.images.dto.ImageDownloadResponse;
import com.tuiop.markethub.products.images.dto.ProductImageResponse;
import com.tuiop.markethub.products.images.exceptions.ProductImageLimitExceededException;
import com.tuiop.markethub.products.images.mapper.ProductImageMapper;
import com.tuiop.markethub.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ObjectStorageService objectStorageService;
    private final ProductImageMapper productImageMapper;
    private final ProductImagesProperties productImagesProperties;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;


    @Transactional(rollbackFor = IOException.class)
    @CacheEvict(value = "public-product", key = "#productId")
    public ProductImageResponse upload(MultipartFile file, UUID productId, Integer position, CustomUserDetails principal)  {

        UUID userId = principal.getUserId();

        Product product = productRepository.findByIdAndUserIdForUpdate(productId, userId).orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));



        long imageCount = productImageRepository.countByProductId(productId);

        if (imageCount >= productImagesProperties.maxImagesPerProduct()) {
            throw new ProductImageLimitExceededException(productImagesProperties.maxImagesPerProduct());
        }

        if (position == null || position < 1 || position > productImagesProperties.maxImagesPerProduct()) {
            throw new IllegalArgumentException();
        }

        validateImage(file);
        String objectKey = UUID.randomUUID() + getExtension(file.getOriginalFilename());


        try {

            objectStorageService.upload(
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );


            ProductImage image = ProductImage.builder()
                    .product(product)
                    .objectKey(objectKey)
                    .contentType(file.getContentType())
                    .sizeBytes( file.getSize())
                    .position(position)
                    .build();

            ProductImage saved = productImageRepository.save(image);
            return productImageMapper.toProductImageResponse(saved);

        } catch (Exception e) {
            objectStorageService.delete(objectKey);
            throw new StorageException("Failed to save the file", e);

        }
    }

    public ImageDownloadResponse download(UUID id) {
        ProductImage image =productImageRepository.findPubliclyVisibleById(id).orElseThrow(() -> new ResourceNotFoundException(ProductImage.class, id));


        Resource resource = new InputStreamResource(objectStorageService.download(image.getObjectKey()));
        return new ImageDownloadResponse(resource, image.getContentType(), image.getObjectKey());

    }


    @Transactional
    @CacheEvict(value = "public-product", key = "#productId")
    public void delete(UUID id, UUID productId, CustomUserDetails principal) {

        UUID userId = principal.getUserId();


        ProductImage image =productImageRepository.findByIdAndUserIdAndProductId(id, userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductImage.class, id));


        productImageRepository.delete(image);
        productImageRepository.flush();
        objectStorageService.delete(image.getObjectKey());


    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        if (file.getSize() > productImagesProperties.maxFileSizeBytes()) {
            throw new FileTooLargeException(productImagesProperties.maxFileSizeBytes());
        }

        if (!productImagesProperties.allowedContentTypes().contains(file.getContentType())) {
            throw new UnsupportedContentTypeException(file.getContentType());
        }

    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));

    }


}
