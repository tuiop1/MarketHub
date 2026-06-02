package com.tuiop.markethub.orders;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class OwnProductPurchaseException extends ConflictException {
    public OwnProductPurchaseException(String message) {
        super(message);
    }
}
