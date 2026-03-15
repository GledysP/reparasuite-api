package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.LoginRequest;
import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.service.AuthService;
import com.reparasuite.api.service.LoginRateLimitService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final LoginRateLimitService rateLimitService;

  public AuthController(AuthService authService, LoginRateLimitService rateLimitService) {
    this.authService = authService;
    this.rateLimitService = rateLimitService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Validated @RequestBody LoginRequest req,
      HttpServletRequest request
  ) {
    String ip = clientIp(request);
    String principal = req.usuario();

    rateLimitService.assertAllowed("BACKOFFICE_LOGIN", principal, ip);

    try {
      LoginResponse response = authService.login(req.usuario(), req.password());
      rateLimitService.recordSuccess("BACKOFFICE_LOGIN", principal, ip);
      return ResponseEntity.ok(response);
    } catch (RuntimeException ex) {
      rateLimitService.recordFailure("BACKOFFICE_LOGIN", principal, ip);
      throw ex;
    }
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}