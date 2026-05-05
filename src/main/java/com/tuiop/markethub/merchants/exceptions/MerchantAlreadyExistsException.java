package com.tuiop.markethub.merchants.exceptions;

public class MerchantAlreadyExistsException extends RuntimeException {
  public MerchantAlreadyExistsException(String message) {
    super(message);
  }
}
