package com.tuiop.markethub.categories.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class CategoryAlreadyExistsException extends ConflictException {
    public CategoryAlreadyExistsException(String name) {
        super("Category with the name: " + name + " already exists");
    }
}
