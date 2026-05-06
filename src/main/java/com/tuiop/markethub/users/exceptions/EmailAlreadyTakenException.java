package com.tuiop.markethub.users.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class EmailAlreadyTakenException extends ConflictException {
    public EmailAlreadyTakenException(String email) {
        super("Email: " + email + " is already taken");
    }
}
