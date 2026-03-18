package com.reparasuite.api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.reparasuite.api.exception.TooManyRequestsException;

@Service
public class LoginRateLimitService {

  private static final int MAX_FAILURES = 5;
  private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
  private static final Duration STALE_ENTRY_TTL = Duration.ofHours(6);

  private final ConcurrentHashMap<String, AttemptState> store = new ConcurrentHashMap<>();

  public void assertAllowed(String scope, String principal, String ip) {
    cleanup();

    AttemptState state = store.get(key(scope, principal, ip));
    if (state == null) return;

    Instant now = Instant.now();
    if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) {
      long retryAfterSeconds = Duration.between(now, state.blockedUntil).toSeconds();
      if (retryAfterSeconds <= 0) {
        retryAfterSeconds = 1;
      }

      throw new TooManyRequestsException(
          "Demasiados intentos fallidos. Intenta más tarde",
          retryAfterSeconds
      );
    }
  }

  public void recordFailure(String scope, String principal, String ip) {
    cleanup();

    String key = key(scope, principal, ip);
    Instant now = Instant.now();

    store.compute(key, (k, current) -> {
      AttemptState state = current == null ? new AttemptState() : current;
      state.failures++;
      state.lastFailureAt = now;

      if (state.failures >= MAX_FAILURES) {
        state.blockedUntil = now.plus(BLOCK_DURATION);
      }

      return state;
    });
  }

  public void recordSuccess(String scope, String principal, String ip) {
    store.remove(key(scope, principal, ip));
  }

  private String key(String scope, String principal, String ip) {
    String p = normalize(principal);
    String i = normalize(ip);
    String s = normalize(scope);
    return s + "|" + p + "|" + i;
  }

  private String normalize(String value) {
    if (value == null) return "";
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private void cleanup() {
    Instant now = Instant.now();
    store.entrySet().removeIf(entry -> {
      AttemptState state = entry.getValue();
      if (state == null) return true;

      if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) {
        return false;
      }

      Instant ref = state.lastFailureAt != null ? state.lastFailureAt : now;
      return ref.plus(STALE_ENTRY_TTL).isBefore(now);
    });
  }

  private static class AttemptState {
    int failures;
    Instant blockedUntil;
    Instant lastFailureAt;
  }
}