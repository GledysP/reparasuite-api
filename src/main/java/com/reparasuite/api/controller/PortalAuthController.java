package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.PortalLoginRequest;
import com.reparasuite.api.dto.PortalLoginResponse;
import com.reparasuite.api.dto.PortalRegisterRequest;
import com.reparasuite.api.dto.PortalRegisterResponse;
import com.reparasuite.api.service.LoginRateLimitService;
import com.reparasuite.api.service.PortalAuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/portal/auth")
public class PortalAuthController {

  private final PortalAuthService service;
  private final LoginRateLimitService rateLimitService;

  public PortalAuthController(PortalAuthService service, LoginRateLimitService rateLimitService) {
    this.service = service;
    this.rateLimitService = rateLimitService;
  }

  @PostMapping("/login")
  public ResponseEntity<PortalLoginResponse> login(
      @Validated @RequestBody PortalLoginRequest req,
      HttpServletRequest request
  ) {
    String ip = clientIp(request);
    String principal = req.email();

    rateLimitService.assertAllowed("PORTAL_LOGIN", principal, ip);

    try {
      String token = service.login(req.email(), req.password());
      rateLimitService.recordSuccess("PORTAL_LOGIN", principal, ip);
      return ResponseEntity.ok(new PortalLoginResponse(token));
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("PORTAL_LOGIN", principal, ip);
      throw ex;
    }
  }

  @PostMapping("/register")
  public ResponseEntity<PortalRegisterResponse> register(@Validated @RequestBody PortalRegisterRequest req) {
    service.register(req.nombre(), req.email(), req.password());
    return ResponseEntity.ok(new PortalRegisterResponse("Cuenta creada correctamente"));
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      String[] parts = forwarded.split(",");
      if (parts.length > 0 && !parts[0].trim().isBlank()) {
        return parts[0].trim();
      }
    }
    return request.getRemoteAddr();
  }
}