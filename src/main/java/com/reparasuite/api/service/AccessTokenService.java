package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AccessTokenService {

  @Value("${reparasuite.jwt.secret}")
  private String secret;

  @Value("${reparasuite.jwt.issuer}")
  private String issuer;

  @Value("${reparasuite.jwt.exp-min}")
  private long expMin;

  public String createToken(UUID subjectId, Map<String, Object> claims) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(Math.max(expMin, 1) * 60);

    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    var builder = Jwts.builder()
        .issuer(issuer)
        .subject(subjectId.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp));

    claims.forEach(builder::claim);

    return builder
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public long getExpiresInSeconds() {
    return Math.max(expMin, 1) * 60;
  }
}