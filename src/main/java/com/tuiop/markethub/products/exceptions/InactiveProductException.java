package com.tuiop.markethub.products.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class InactiveProductException extends ConflictException {
    public InactiveProductException(String name) {
        super("Product \""+ name + "\" is inactive");
    }
}
