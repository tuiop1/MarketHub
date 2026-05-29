package com.tuiop.markethub.common.storage.object.exceptions;

public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(long maxSizeBytes) {
        super("File exceeds maximum allowed size of " + maxSizeBytes + " bytes");
    }
}
