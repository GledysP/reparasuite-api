package com.reparasuite.api.exception;

public abstract class ApiException extends RuntimeException {

  private final int status;

  protected ApiException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}