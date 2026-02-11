package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.UsuarioRepo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

  private final UsuarioRepo usuarioRepo;
  private final PasswordEncoder passwordEncoder;

  @Value("${reparasuite.jwt.secret}")
  private String secret;

  @Value("${reparasuite.jwt.issuer}")
  private String issuer;

  @Value("${reparasuite.jwt.exp-min}")
  private long expMin;

  public AuthService(UsuarioRepo usuarioRepo, PasswordEncoder passwordEncoder) {
    this.usuarioRepo = usuarioRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public LoginResponse login(String usuario, String password) {
    Usuario u = usuarioRepo.findByUsuario(usuario)
        .filter(Usuario::isActivo)
        .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

    if (!passwordEncoder.matches(password, u.getPasswordHash())) {
      throw new RuntimeException("Credenciales inválidas");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMin * 60);

    // HS256 necesita clave >= 256 bits (32 bytes) mínimo
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    String token = Jwts.builder()
        .issuer(issuer)
        .subject(u.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("usuario", u.getUsuario())
        .claim("rol", u.getRol().name())
        .signWith(key, Jwts.SIG.HS256)
        .compact();

    return new LoginResponse(token, toDto(u));
  }

  private UsuarioResumenDto toDto(Usuario u) {
    return new UsuarioResumenDto(u.getId(), u.getNombre(), u.getUsuario(), u.getRol().name(), u.isActivo());
  }
}
