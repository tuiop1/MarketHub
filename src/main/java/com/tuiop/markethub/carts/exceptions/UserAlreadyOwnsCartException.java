package com.tuiop.markethub.carts.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class UserAlreadyOwnsCartException extends ConflictException {
    public UserAlreadyOwnsCartException() {
        super("User already has a cart");
    }
}
