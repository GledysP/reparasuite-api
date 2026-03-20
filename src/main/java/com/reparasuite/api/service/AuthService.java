package com.reparasuite.api.service;

import java.util.Map;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.LoginResponse;
import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.exception.UnauthorizedException;
import com.reparasuite.api.model.RefreshToken;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class AuthService {

  private static final String SUBJECT_TYPE = "BACKOFFICE";

  private final UsuarioRepo usuarioRepo;
  private final PasswordEncoder passwordEncoder;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;

  public AuthService(
      UsuarioRepo usuarioRepo,
      PasswordEncoder passwordEncoder,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService
  ) {
    this.usuarioRepo = usuarioRepo;
    this.passwordEncoder = passwordEncoder;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
  }

  public LoginResponse login(String usuario, String password, String ip, String userAgent) {
    Usuario u = usuarioRepo.findByUsuario(usuario)
        .filter(Usuario::isActivo)
        .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

    if (!passwordEncoder.matches(password, u.getPasswordHash())) {
      throw new UnauthorizedException("Credenciales inválidas");
    }

    String accessToken = createAccessToken(u);
    String refreshToken = refreshTokenService.createToken(u.getId(), SUBJECT_TYPE, ip, userAgent);

    return new LoginResponse(
        accessToken,
        refreshToken,
        accessTokenService.getExpiresInSeconds(),
        toDto(u)
    );
  }

  public LoginResponse refresh(String rawRefreshToken, String ip, String userAgent) {
    RefreshToken rotated = refreshTokenService.rotateToken(rawRefreshToken, ip, userAgent);

    if (!SUBJECT_TYPE.equalsIgnoreCase(rotated.getSubjectType())) {
      throw new UnauthorizedException("Refresh token inválido para backoffice");
    }

    Usuario u = usuarioRepo.findById(rotated.getSubjectId())
        .filter(Usuario::isActivo)
        .orElseThrow(() -> new UnauthorizedException("Usuario no válido"));

    String accessToken = createAccessToken(u);
    String newRawRefreshToken = rotated.getReplacedByTokenHash();

    return new LoginResponse(
        accessToken,
        newRawRefreshToken,
        accessTokenService.getExpiresInSeconds(),
        toDto(u)
    );
  }

  public void logout(String rawRefreshToken) {
    refreshTokenService.revokeToken(rawRefreshToken);
  }

  private String createAccessToken(Usuario u) {
    return accessTokenService.createToken(
        u.getId(),
        Map.of(
            "usuario", u.getUsuario(),
            "nombre", u.getNombre(),
            "rol", u.getRol().name()
        )
    );
  }

  private UsuarioResumenDto toDto(Usuario u) {
    return new UsuarioResumenDto(
        u.getId(),
        u.getNombre(),
        u.getUsuario(),
        u.getEmail(),
        u.getRol().name(),
        u.isActivo()
    );
  }
}