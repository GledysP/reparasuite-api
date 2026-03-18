package com.reparasuite.api.support;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  TestJwtFactory testJwtFactory(
      @Value("${reparasuite.jwt.secret}") String secret,
      @Value("${reparasuite.jwt.issuer}") String issuer
  ) {
    return new TestJwtFactory(secret, issuer);
  }

  public static class TestJwtFactory {
    private final String secret;
    private final String issuer;

    public TestJwtFactory(String secret, String issuer) {
      this.secret = secret;
      this.issuer = issuer;
    }

    public String backofficeToken(UUID userId, String usuario, String nombre, String rol) {
      Instant now = Instant.now();
      Instant exp = now.plusSeconds(3600);

      return Jwts.builder()
          .issuer(issuer)
          .subject(userId.toString())
          .issuedAt(Date.from(now))
          .expiration(Date.from(exp))
          .claim("usuario", usuario)
          .claim("nombre", nombre)
          .claim("rol", rol)
          .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
          .compact();
    }

    public String clienteToken(UUID clienteId, String email, String nombre) {
      Instant now = Instant.now();
      Instant exp = now.plusSeconds(3600);

      return Jwts.builder()
          .issuer(issuer)
          .subject(clienteId.toString())
          .issuedAt(Date.from(now))
          .expiration(Date.from(exp))
          .claim("email", email)
          .claim("nombre", nombre)
          .claim("rol", "CLIENTE")
          .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
          .compact();
    }
  }
}