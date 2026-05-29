package com.tuiop.markethub.common.storage.object.exceptions;

import com.tuiop.markethub.common.exceptions.ConflictException;

public class StorageException extends RuntimeException{
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(String message){
        super(message);
    }
}
