package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.PortalLoginRequest;
import com.reparasuite.api.dto.PortalLoginResponse;
import com.reparasuite.api.dto.PortalRegisterRequest;
import com.reparasuite.api.dto.PortalRegisterResponse;
import com.reparasuite.api.service.ClientIpService;
import com.reparasuite.api.service.LoginRateLimitService;
import com.reparasuite.api.service.PortalAuthService;
import com.reparasuite.api.service.RefreshTokenCookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/portal/auth")
public class PortalAuthController {

  private final PortalAuthService service;
  private final LoginRateLimitService rateLimitService;
  private final ClientIpService clientIpService;
  private final RefreshTokenCookieService refreshTokenCookieService;

  public PortalAuthController(
      PortalAuthService service,
      LoginRateLimitService rateLimitService,
      ClientIpService clientIpService,
      RefreshTokenCookieService refreshTokenCookieService
  ) {
    this.service = service;
    this.rateLimitService = rateLimitService;
    this.clientIpService = clientIpService;
    this.refreshTokenCookieService = refreshTokenCookieService;
  }

  @PostMapping("/login")
  public ResponseEntity<PortalLoginResponse> login(
      @Validated @RequestBody PortalLoginRequest req,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String ip = clientIpService.resolve(request);
    String principal = req.email();
    String userAgent = request.getHeader("User-Agent");

    rateLimitService.assertAllowed("PORTAL_LOGIN", principal, ip);

    try {
      PortalLoginResponse login = service.login(req.email(), req.password(), ip, userAgent);
      rateLimitService.recordSuccess("PORTAL_LOGIN", principal, ip);

      refreshTokenCookieService.addRefreshTokenCookie(response, login.refreshToken());

      PortalLoginResponse body = new PortalLoginResponse(
          login.accessToken(),
          null,
          login.expiresInSeconds()
      );

      return ResponseEntity.ok(body);
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("PORTAL_LOGIN", principal, ip);
      throw ex;
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<PortalLoginResponse> refresh(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String ip = clientIpService.resolve(request);
    String userAgent = request.getHeader("User-Agent");
    String refreshToken = refreshTokenCookieService.extractRefreshToken(request);

    rateLimitService.assertAllowed("REFRESH_TOKEN", "portal", ip);

    try {
      PortalLoginResponse refreshed = service.refresh(refreshToken, ip, userAgent);
      rateLimitService.recordSuccess("REFRESH_TOKEN", "portal", ip);

      refreshTokenCookieService.addRefreshTokenCookie(response, refreshed.refreshToken());

      PortalLoginResponse body = new PortalLoginResponse(
          refreshed.accessToken(),
          null,
          refreshed.expiresInSeconds()
      );

      return ResponseEntity.ok(body);
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("REFRESH_TOKEN", "portal", ip);
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
      service.logout(refreshToken);
    }
    refreshTokenCookieService.clearRefreshTokenCookie(response);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/register")
  public ResponseEntity<PortalRegisterResponse> register(@Validated @RequestBody PortalRegisterRequest req) {
    service.register(req.nombre(), req.email(), req.password(), req.telefono());
    return ResponseEntity.ok(new PortalRegisterResponse("Cuenta creada correctamente"));
  }
}