package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.PortalLoginRequest;
import com.reparasuite.api.dto.PortalLoginResponse;
import com.reparasuite.api.service.PortalAuthService;

@RestController
@RequestMapping("/api/v1/portal/auth")
public class PortalAuthController {

  private final PortalAuthService service;

  public PortalAuthController(PortalAuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public ResponseEntity<PortalLoginResponse> login(@Validated @RequestBody PortalLoginRequest req) {
    return ResponseEntity.ok(new PortalLoginResponse(service.login(req.email(), req.password())));
  }
}
