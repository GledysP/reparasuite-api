package com.reparasuite.api.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.reparasuite.api.exception.UnauthorizedException;

@Service
public class AuthContextService {

  public AuthUser current() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
      throw new UnauthorizedException("No autenticado");
    }

    String rol = claim(jwtAuth, "rol");
    String nombre = claim(jwtAuth, "nombre");
    String usuario = claim(jwtAuth, "usuario");
    String sub = jwtAuth.getToken().getSubject();

    UUID id;
    try {
      id = UUID.fromString(sub);
    } catch (Exception e) {
      throw new UnauthorizedException("Token inválido");
    }

    return new AuthUser(id, rol, nombre, usuario);
  }

  private String claim(JwtAuthenticationToken auth, String key) {
    Object v = auth.getToken().getClaims().get(key);
    return v == null ? null : String.valueOf(v);
  }

  public record AuthUser(
      UUID id,
      String rol,
      String nombre,
      String usuario
  ) {
    public boolean isCliente() {
      return "CLIENTE".equalsIgnoreCase(rol);
    }

    public boolean isBackoffice() {
      return "ADMIN".equalsIgnoreCase(rol) || "TECNICO".equalsIgnoreCase(rol);
    }

    public String displayName() {
      if (nombre != null && !nombre.isBlank()) return nombre;
      if (usuario != null && !usuario.isBlank()) return usuario;
      return "Usuario";
    }
  }
}