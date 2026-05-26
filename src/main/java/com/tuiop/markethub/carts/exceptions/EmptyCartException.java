package com.tuiop.markethub.carts.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class EmptyCartException extends ConflictException {
    public EmptyCartException() {
        super("The cart is empty");
    }
}
