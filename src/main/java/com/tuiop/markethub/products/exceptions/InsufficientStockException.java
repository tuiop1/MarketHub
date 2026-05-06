package com.tuiop.markethub.products.exceptions;


import com.tuiop.markethub.common.exceptions.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(String productName, int requestedQuantity, int availableQuantity) {
        super("Not enough stock for product '%s'. Requested: %d, available: %d"
                .formatted(productName, requestedQuantity, availableQuantity));
    }
}