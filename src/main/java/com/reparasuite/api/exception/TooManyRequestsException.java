package com.reparasuite.api.exception;

public class TooManyRequestsException extends ApiException {

  public TooManyRequestsException(String message) {
    super(429, message);
  }
}