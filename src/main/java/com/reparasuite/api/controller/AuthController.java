package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.LoginRequest;
import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.service.AuthService;
import com.reparasuite.api.service.ClientIpService;
import com.reparasuite.api.service.LoginRateLimitService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final LoginRateLimitService rateLimitService;
  private final ClientIpService clientIpService;

  public AuthController(
      AuthService authService,
      LoginRateLimitService rateLimitService,
      ClientIpService clientIpService
  ) {
    this.authService = authService;
    this.rateLimitService = rateLimitService;
    this.clientIpService = clientIpService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Validated @RequestBody LoginRequest req,
      HttpServletRequest request
  ) {
    String ip = clientIpService.resolve(request);
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
}