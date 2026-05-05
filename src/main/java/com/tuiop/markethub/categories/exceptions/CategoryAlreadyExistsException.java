package com.tuiop.markethub.categories.exceptions;

public class CategoryAlreadyExistsException extends RuntimeException {
  public CategoryAlreadyExistsException(String message) {
    super(message);
  }
}
