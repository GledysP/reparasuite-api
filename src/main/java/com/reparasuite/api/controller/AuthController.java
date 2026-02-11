package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.LoginRequest;
import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest req) {
    return ResponseEntity.ok(authService.login(req.usuario(), req.password()));
  }
}
