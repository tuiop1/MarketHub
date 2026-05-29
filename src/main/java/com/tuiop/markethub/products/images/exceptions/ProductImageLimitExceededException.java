package com.tuiop.markethub.products.images.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class ProductImageLimitExceededException extends ConflictException {
    public ProductImageLimitExceededException(Integer limit) {
        super("The limit of" + limit + " images per product was exceeded");
    }


    public ProductImageLimitExceededException(String message) {
        super(message);
    }
}
