package com.reparasuite.api.exception;

public class TooManyRequestsException extends ApiException {

  private final long retryAfterSeconds;

  public TooManyRequestsException(String message, long retryAfterSeconds) {
    super(429, message);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}