package com.tuiop.markethub.common.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> resourceClass, String fieldName, Object fieldValue) {
        super(resourceClass.getSimpleName() + " with " + fieldName + ": " + fieldValue + " was not found");

    }

    public ResourceNotFoundException(Class<?> resourceClass, Object id) {
        super(resourceClass.getSimpleName() + " with id: " + id + " was not found");

    }





}
