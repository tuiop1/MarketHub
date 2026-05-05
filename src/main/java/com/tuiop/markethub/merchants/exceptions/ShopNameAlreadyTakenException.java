package com.tuiop.markethub.merchants.exceptions;

public class ShopNameAlreadyTakenException extends RuntimeException {
  public ShopNameAlreadyTakenException(String message) {
    super(message);
  }
}
