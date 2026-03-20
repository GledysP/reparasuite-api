package com.reparasuite.api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.reparasuite.api.config.LoginRateLimitProperties;
import com.reparasuite.api.exception.TooManyRequestsException;

@Service
public class LoginRateLimitService {

  private static final Logger log = LoggerFactory.getLogger(LoginRateLimitService.class);

  private final ConcurrentHashMap<String, AttemptState> store = new ConcurrentHashMap<>();
  private final LoginRateLimitProperties properties;

  public LoginRateLimitService(LoginRateLimitProperties properties) {
    this.properties = properties;
  }

  public void assertAllowed(String scope, String principal, String ip) {
    cleanup(scope);

    AttemptState state = store.get(key(scope, principal, ip));
    if (state == null) return;

    Instant now = Instant.now();
    if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) {
      long retryAfterSeconds = Duration.between(now, state.blockedUntil).toSeconds();
      if (retryAfterSeconds <= 0) {
        retryAfterSeconds = 1;
      }

      log.warn(
          "Bloqueado scope={} principal={} ip={} retryAfterSeconds={}",
          normalize(scope), normalize(principal), normalize(ip), retryAfterSeconds
      );

      throw new TooManyRequestsException(
          "Demasiados intentos fallidos. Intenta más tarde",
          retryAfterSeconds
      );
    }
  }

  public void recordFailure(String scope, String principal, String ip) {
    cleanup(scope);

    String key = key(scope, principal, ip);
    Instant now = Instant.now();
    ScopeConfig cfg = configFor(scope);

    store.compute(key, (k, current) -> {
      AttemptState state = current == null ? new AttemptState() : current;
      state.failures++;
      state.lastFailureAt = now;

      if (state.failures >= cfg.maxFailures()) {
        state.blockedUntil = now.plus(cfg.blockDuration());
        log.warn(
            "Rate limited scope={} principal={} ip={} failures={} blockedUntil={}",
            normalize(scope), normalize(principal), normalize(ip), state.failures, state.blockedUntil
        );
      } else {
        log.info(
            "Fallo auth scope={} principal={} ip={} failures={}",
            normalize(scope), normalize(principal), normalize(ip), state.failures
        );
      }

      return state;
    });
  }

  public void recordSuccess(String scope, String principal, String ip) {
    store.remove(key(scope, principal, ip));
  }

  private String key(String scope, String principal, String ip) {
    return normalize(scope) + "|" + normalize(principal) + "|" + normalize(ip);
  }

  private String normalize(String value) {
    if (value == null) return "";
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private void cleanup(String scope) {
    ScopeConfig cfg = configFor(scope);
    Instant now = Instant.now();

    store.entrySet().removeIf(entry -> {
      AttemptState state = entry.getValue();
      if (state == null) return true;
      if (!entry.getKey().startsWith(normalize(scope) + "|")) return false;
      if (state.blockedUntil != null && state.blockedUntil.isAfter(now)) return false;

      Instant ref = state.lastFailureAt != null ? state.lastFailureAt : now;
      return ref.plus(cfg.staleTtl()).isBefore(now);
    });
  }

  private ScopeConfig configFor(String scope) {
    String normalized = normalize(scope);

    LoginRateLimitProperties.Scope cfg;
    if ("portal_login".equals(normalized)) {
      cfg = properties.getPortal();
    } else if ("refresh_token".equals(normalized)) {
      cfg = properties.getRefresh();
    } else {
      cfg = properties.getBackoffice();
    }

    int maxFailures = Math.max(cfg.getMaxFailures(), 1);
    Duration blockDuration = Duration.ofMinutes(Math.max(cfg.getBlockDurationMinutes(), 1));
    Duration staleTtl = Duration.ofHours(Math.max(cfg.getStaleEntryHours(), 1));

    return new ScopeConfig(maxFailures, blockDuration, staleTtl);
  }

  private record ScopeConfig(int maxFailures, Duration blockDuration, Duration staleTtl) {
  }

  private static class AttemptState {
    int failures;
    Instant blockedUntil;
    Instant lastFailureAt;
  }
}