package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    String emailNorm = normalizarEmail(email);

    Cliente c = clienteRepo.findByEmailIgnoreCase(emailNorm)
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
        .claim("nombre", c.getNombre())
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  @Transactional
  public void register(String nombre, String email, String password) {
    String nombreNorm = limpiar(nombre);
    String emailNorm = normalizarEmail(email);
    String passwordNorm = password == null ? null : password.trim();

    if (nombreNorm == null || nombreNorm.length() < 2) {
      throw new RuntimeException("El nombre es inválido");
    }

    if (emailNorm == null) {
      throw new RuntimeException("El email es inválido");
    }

    if (passwordNorm == null || passwordNorm.length() < 6) {
      throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
    }

    Cliente cliente = clienteRepo.findByEmailIgnoreCase(emailNorm).orElse(null);

    if (cliente == null) {
      cliente = new Cliente();
      cliente.setNombre(nombreNorm);
      cliente.setEmail(emailNorm);
      cliente.setTelefono(null);
      cliente.setPortalActivo(true);
      cliente.setPasswordHashPortal(encoder.encode(passwordNorm));
      clienteRepo.save(cliente);
      return;
    }

    if (cliente.isPortalActivo() && cliente.getPasswordHashPortal() != null && !cliente.getPasswordHashPortal().isBlank()) {
      throw new RuntimeException("Ya existe una cuenta activa con ese email");
    }

    if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
      cliente.setNombre(nombreNorm);
    }

    cliente.setEmail(emailNorm);
    cliente.setPortalActivo(true);
    cliente.setPasswordHashPortal(encoder.encode(passwordNorm));

    clienteRepo.save(cliente);
  }

  private String limpiar(String value) {
    if (value == null) return null;
    String out = value.trim();
    return out.isBlank() ? null : out;
  }

  private String normalizarEmail(String email) {
    if (email == null) return null;
    String out = email.trim().toLowerCase(Locale.ROOT);
    return out.isBlank() ? null : out;
  }
}