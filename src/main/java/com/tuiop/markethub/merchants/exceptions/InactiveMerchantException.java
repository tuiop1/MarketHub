package com.tuiop.markethub.merchants.exceptions;

public class InactiveMerchantException extends RuntimeException {
    public InactiveMerchantException(String shopName ) {
        super("Shop \"" + shopName + "\" is inactive");
    }
}
