package com.tuiop.markethub.common.storage.object.exceptions;

public class UnsupportedContentTypeException extends RuntimeException {

    public UnsupportedContentTypeException(String contentType) {
        super("Unsupported image content type: " + contentType);
    }

    public UnsupportedContentTypeException() {
        super("Unsupported image content type");
    }
}
