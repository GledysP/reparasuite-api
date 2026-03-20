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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/portal/auth")
public class PortalAuthController {

  private final PortalAuthService service;
  private final LoginRateLimitService rateLimitService;
  private final ClientIpService clientIpService;

  public PortalAuthController(
      PortalAuthService service,
      LoginRateLimitService rateLimitService,
      ClientIpService clientIpService
  ) {
    this.service = service;
    this.rateLimitService = rateLimitService;
    this.clientIpService = clientIpService;
  }

  @PostMapping("/login")
  public ResponseEntity<PortalLoginResponse> login(
      @Validated @RequestBody PortalLoginRequest req,
      HttpServletRequest request
  ) {
    String ip = clientIpService.resolve(request);
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
}