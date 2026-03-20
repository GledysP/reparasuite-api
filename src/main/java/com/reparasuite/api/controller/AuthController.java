package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.LoginRequest;
import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.service.AuthService;
import com.reparasuite.api.service.ClientIpService;
import com.reparasuite.api.service.LoginRateLimitService;
import com.reparasuite.api.service.RefreshTokenCookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final LoginRateLimitService rateLimitService;
  private final ClientIpService clientIpService;
  private final RefreshTokenCookieService refreshTokenCookieService;

  public AuthController(
      AuthService authService,
      LoginRateLimitService rateLimitService,
      ClientIpService clientIpService,
      RefreshTokenCookieService refreshTokenCookieService
  ) {
    this.authService = authService;
    this.rateLimitService = rateLimitService;
    this.clientIpService = clientIpService;
    this.refreshTokenCookieService = refreshTokenCookieService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Validated @RequestBody LoginRequest req,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String ip = clientIpService.resolve(request);
    String principal = req.usuario();
    String userAgent = request.getHeader("User-Agent");

    rateLimitService.assertAllowed("BACKOFFICE_LOGIN", principal, ip);

    try {
      LoginResponse login = authService.login(req.usuario(), req.password(), ip, userAgent);
      rateLimitService.recordSuccess("BACKOFFICE_LOGIN", principal, ip);

      refreshTokenCookieService.addRefreshTokenCookie(response, login.refreshToken());

      LoginResponse body = new LoginResponse(
          login.accessToken(),
          null,
          login.expiresInSeconds(),
          login.usuario()
      );

      return ResponseEntity.ok(body);
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("BACKOFFICE_LOGIN", principal, ip);
      throw ex;
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String ip = clientIpService.resolve(request);
    String userAgent = request.getHeader("User-Agent");
    String refreshToken = refreshTokenCookieService.extractRefreshToken(request);

    rateLimitService.assertAllowed("REFRESH_TOKEN", "backoffice", ip);

    try {
      LoginResponse refreshed = authService.refresh(refreshToken, ip, userAgent);
      rateLimitService.recordSuccess("REFRESH_TOKEN", "backoffice", ip);

      refreshTokenCookieService.addRefreshTokenCookie(response, refreshed.refreshToken());

      LoginResponse body = new LoginResponse(
          refreshed.accessToken(),
          null,
          refreshed.expiresInSeconds(),
          refreshed.usuario()
      );

      return ResponseEntity.ok(body);
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("REFRESH_TOKEN", "backoffice", ip);
      throw ex;
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String refreshToken = refreshTokenCookieService.extractRefreshToken(request);
    if (refreshToken != null && !refreshToken.isBlank()) {
      authService.logout(refreshToken);
    }
    refreshTokenCookieService.clearRefreshTokenCookie(response);
    return ResponseEntity.noContent().build();
  }
}