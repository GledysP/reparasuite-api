package com.reparasuite.api.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Component
public class JwtStartupValidator {

  @Value("${reparasuite.jwt.secret}")
  private String secret;

  @Value("${reparasuite.jwt.issuer}")
  private String issuer;

  @Value("${reparasuite.jwt.exp-min}")
  private long expMin;

  @PostConstruct
  void validate() {
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("La propiedad reparasuite.jwt.secret es obligatoria");
    }

    int secretBytes = secret.getBytes(StandardCharsets.UTF_8).length;
    if (secretBytes < 32) {
      throw new IllegalStateException(
          "La propiedad reparasuite.jwt.secret debe tener al menos 32 bytes para HS256"
      );
    }

    if (!StringUtils.hasText(issuer)) {
      throw new IllegalStateException("La propiedad reparasuite.jwt.issuer es obligatoria");
    }

    if (expMin <= 0) {
      throw new IllegalStateException("La propiedad reparasuite.jwt.exp-min debe ser mayor que 0");
    }
  }
}