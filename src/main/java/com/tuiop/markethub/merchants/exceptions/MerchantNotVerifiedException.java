package com.tuiop.markethub.merchants.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class MerchantNotVerifiedException extends ConflictException {
    public MerchantNotVerifiedException() {
        super("Merchant must be verified to manage products");
    }
}
