package com.tuiop.markethub.categories.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class InactiveCategoryException extends ConflictException {
    public InactiveCategoryException(String name) {
        super(name + "category is inactive");
    }
}
