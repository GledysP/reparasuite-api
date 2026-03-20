package com.reparasuite.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.reparasuite.api.config.RefreshTokenCookieProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class RefreshTokenCookieService {

  private final RefreshTokenCookieProperties properties;

  public RefreshTokenCookieService(RefreshTokenCookieProperties properties) {
    this.properties = properties;
  }

  public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), refreshToken)
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .path(properties.getCookiePath())
        .sameSite(properties.getCookieSameSite())
        .maxAge(properties.getExpDays() * 24 * 60 * 60)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public void clearRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), "")
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .path(properties.getCookiePath())
        .sameSite(properties.getCookieSameSite())
        .maxAge(0)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public String extractRefreshToken(HttpServletRequest request) {
    if (request == null || request.getCookies() == null) {
      return null;
    }

    for (var cookie : request.getCookies()) {
      if (properties.getCookieName().equals(cookie.getName())) {
        String value = cookie.getValue();
        if (value != null && !value.isBlank()) {
          return value;
        }
      }
    }

    return null;
  }
}