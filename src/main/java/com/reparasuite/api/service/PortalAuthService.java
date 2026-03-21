package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.PortalLoginResponse;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.UnauthorizedException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.repo.ClienteRepo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class PortalAuthService {

  private static final String SUBJECT_TYPE_CLIENTE = "CLIENTE";

  private final ClienteRepo clienteRepo;
  private final PasswordEncoder encoder;
  private final RefreshTokenService refreshTokenService;

  @Value("${reparasuite.jwt.secret}")
  private String secret;

  @Value("${reparasuite.jwt.issuer}")
  private String issuer;

  @Value("${reparasuite.jwt.exp-min}")
  private long expMin;

  public PortalAuthService(
      ClienteRepo clienteRepo,
      PasswordEncoder encoder,
      RefreshTokenService refreshTokenService
  ) {
    this.clienteRepo = clienteRepo;
    this.encoder = encoder;
    this.refreshTokenService = refreshTokenService;
  }

  public PortalLoginResponse login(String email, String password, String ip, String userAgent) {
    String emailNorm = normalizarEmail(email);

    Cliente c = clienteRepo.findByEmailIgnoreCase(emailNorm)
        .filter(Cliente::isPortalActivo)
        .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

    if (c.getPasswordHashPortal() == null || !encoder.matches(password, c.getPasswordHashPortal())) {
      throw new UnauthorizedException("Credenciales inválidas");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMin * 60);
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    String accessToken = Jwts.builder()
        .issuer(issuer)
        .subject(c.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("rol", "CLIENTE")
        .claim("email", c.getEmail())
        .claim("nombre", c.getNombre())
        .signWith(key, Jwts.SIG.HS256)
        .compact();

    String refreshToken = refreshTokenService.createToken(
        c.getId(),
        SUBJECT_TYPE_CLIENTE,
        ip,
        userAgent
    );

    return new PortalLoginResponse(accessToken, refreshToken, expMin * 60);
  }

  public PortalLoginResponse refresh(String rawRefreshToken, String ip, String userAgent) {
    var rotated = refreshTokenService.rotateToken(rawRefreshToken, ip, userAgent);

    Cliente c = clienteRepo.findById(rotated.getSubjectId())
        .filter(Cliente::isPortalActivo)
        .orElseThrow(() -> new UnauthorizedException("Cliente no disponible"));

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expMin * 60);
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    String accessToken = Jwts.builder()
        .issuer(issuer)
        .subject(c.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("rol", "CLIENTE")
        .claim("email", c.getEmail())
        .claim("nombre", c.getNombre())
        .signWith(key, Jwts.SIG.HS256)
        .compact();

    return new PortalLoginResponse(
        accessToken,
        rotated.getReplacedByTokenHash(),
        expMin * 60
    );
  }

  public void logout(String rawRefreshToken) {
    refreshTokenService.revokeToken(rawRefreshToken);
  }

  @Transactional
  public void register(String nombre, String email, String password, String telefono) {
    String nombreNorm = limpiar(nombre);
    String emailNorm = normalizarEmail(email);
    String passwordNorm = password == null ? null : password.trim();
    String telefonoNorm = limpiarTelefono(telefono);

    if (nombreNorm == null || nombreNorm.length() < 2) {
      throw new BadRequestException("El nombre es inválido");
    }

    if (emailNorm == null) {
      throw new BadRequestException("El email es inválido");
    }

    if (passwordNorm == null || passwordNorm.length() < 6) {
      throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
    }

    if (telefonoNorm == null || telefonoNorm.length() < 7) {
      throw new BadRequestException("El teléfono es inválido");
    }

    Cliente cliente = clienteRepo.findByEmailIgnoreCase(emailNorm).orElse(null);

    if (cliente == null) {
      cliente = new Cliente();
      cliente.setNombre(nombreNorm);
      cliente.setEmail(emailNorm);
      cliente.setTelefono(telefonoNorm);
      cliente.setPortalActivo(true);
      cliente.setPasswordHashPortal(encoder.encode(passwordNorm));
      clienteRepo.save(cliente);
      return;
    }

    if (cliente.isPortalActivo()
        && cliente.getPasswordHashPortal() != null
        && !cliente.getPasswordHashPortal().isBlank()) {
      throw new ConflictException("Ya existe una cuenta activa con ese email");
    }

    if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
      cliente.setNombre(nombreNorm);
    }

    cliente.setEmail(emailNorm);
    cliente.setTelefono(telefonoNorm);
    cliente.setPortalActivo(true);
    cliente.setPasswordHashPortal(encoder.encode(passwordNorm));

    clienteRepo.save(cliente);
  }

  private String limpiar(String value) {
    if (value == null) return null;
    String out = value.trim();
    return out.isBlank() ? null : out;
  }

  private String limpiarTelefono(String value) {
    if (value == null) return null;
    String out = value.trim();
    if (out.isBlank()) return null;
    return out.length() <= 30 ? out : out.substring(0, 30);
  }

  private String normalizarEmail(String email) {
    if (email == null) return null;
    String out = email.trim().toLowerCase(Locale.ROOT);
    return out.isBlank() ? null : out;
  }
}