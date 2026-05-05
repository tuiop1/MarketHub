package com.tuiop.markethub.common.exceptions;

public class EmailAlreadyTakenException extends ConflictException {
    public EmailAlreadyTakenException(String email) {
        super("Email: " + email + " is already taken");
    }
}
