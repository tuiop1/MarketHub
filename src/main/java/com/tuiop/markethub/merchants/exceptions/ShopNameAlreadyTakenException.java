package com.tuiop.markethub.merchants.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class ShopNameAlreadyTakenException extends ConflictException {
    public ShopNameAlreadyTakenException(String shopName) {
        super("Shop name: " + shopName + " is already taken");
    }
}
