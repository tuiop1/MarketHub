package com.tuiop.markethub.merchants.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class MerchantAlreadyExistsException extends ConflictException {
    public MerchantAlreadyExistsException() {
        super("User is already merchant");
    }
}
