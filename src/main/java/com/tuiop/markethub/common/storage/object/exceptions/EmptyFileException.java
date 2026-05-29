package com.tuiop.markethub.common.storage.object.exceptions;

public class EmptyFileException extends RuntimeException {

    public EmptyFileException() {
        super("Image file is empty");
    }
}
