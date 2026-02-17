package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.repo.ClienteRepo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class PortalAuthService {

  private final ClienteRepo clienteRepo;
  private final PasswordEncoder encoder;

  @Value("${reparasuite.jwt.secret}")
  private String secret;

  @Value("${reparasuite.jwt.issuer}")
  private String issuer;

  @Value("${reparasuite.jwt.exp-min}")
  private long expMin;

  public PortalAuthService(ClienteRepo clienteRepo, PasswordEncoder encoder) {
    this.clienteRepo = clienteRepo;
    this.encoder = encoder;
  }

  public String login(String email, String password) {
    Cliente c = clienteRepo.findByEmailIgnoreCase(email)
        .filter(Cliente::isPortalActivo)
        .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

    if (c.getPasswordHashPortal() == null || !encoder.matches(password, c.getPasswordHashPortal())) {
      throw new RuntimeException("Credenciales inválidas");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMin * 60);
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .issuer(issuer)
        .subject(c.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("rol", "CLIENTE")
        .claim("email", c.getEmail())
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }
}
