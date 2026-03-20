package com.reparasuite.api.service;

import java.util.Locale;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.PortalLoginResponse;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.UnauthorizedException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.RefreshToken;
import com.reparasuite.api.repo.ClienteRepo;

@Service
public class PortalAuthService {

  private static final String SUBJECT_TYPE = "CLIENTE";

  private final ClienteRepo clienteRepo;
  private final PasswordEncoder encoder;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;

  public PortalAuthService(
      ClienteRepo clienteRepo,
      PasswordEncoder encoder,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService
  ) {
    this.clienteRepo = clienteRepo;
    this.encoder = encoder;
    this.accessTokenService = accessTokenService;
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

    String accessToken = createAccessToken(c);
    String refreshToken = refreshTokenService.createToken(c.getId(), SUBJECT_TYPE, ip, userAgent);

    return new PortalLoginResponse(
        accessToken,
        refreshToken,
        accessTokenService.getExpiresInSeconds()
    );
  }

  public PortalLoginResponse refresh(String rawRefreshToken, String ip, String userAgent) {
    RefreshToken rotated = refreshTokenService.rotateToken(rawRefreshToken, ip, userAgent);

    if (!SUBJECT_TYPE.equalsIgnoreCase(rotated.getSubjectType())) {
      throw new UnauthorizedException("Refresh token inválido para cliente");
    }

    Cliente c = clienteRepo.findById(rotated.getSubjectId())
        .filter(Cliente::isPortalActivo)
        .orElseThrow(() -> new UnauthorizedException("Cliente no válido"));

    String accessToken = createAccessToken(c);
    String newRawRefreshToken = rotated.getReplacedByTokenHash();

    return new PortalLoginResponse(
        accessToken,
        newRawRefreshToken,
        accessTokenService.getExpiresInSeconds()
    );
  }

  public void logout(String rawRefreshToken) {
    refreshTokenService.revokeToken(rawRefreshToken);
  }

  @Transactional
  public void register(String nombre, String email, String password) {
    String nombreNorm = limpiar(nombre);
    String emailNorm = normalizarEmail(email);
    String passwordNorm = password == null ? null : password.trim();

    if (nombreNorm == null || nombreNorm.length() < 2) {
      throw new BadRequestException("El nombre es inválido");
    }

    if (emailNorm == null) {
      throw new BadRequestException("El email es inválido");
    }

    if (passwordNorm == null || passwordNorm.length() < 6) {
      throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
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

    if (cliente.isPortalActivo()
        && cliente.getPasswordHashPortal() != null
        && !cliente.getPasswordHashPortal().isBlank()) {
      throw new ConflictException("Ya existe una cuenta activa con ese email");
    }

    if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
      cliente.setNombre(nombreNorm);
    }

    cliente.setEmail(emailNorm);
    cliente.setPortalActivo(true);
    cliente.setPasswordHashPortal(encoder.encode(passwordNorm));

    clienteRepo.save(cliente);
  }

  private String createAccessToken(Cliente c) {
    return accessTokenService.createToken(
        c.getId(),
        Map.of(
            "rol", "CLIENTE",
            "email", c.getEmail(),
            "nombre", c.getNombre()
        )
    );
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