package com.tuiop.markethub.merchants.exceptions;

public class MerchantNotVerifiedException extends RuntimeException {
    public MerchantNotVerifiedException() {
        super("Merchant must be verified to manage products");
    }
}
