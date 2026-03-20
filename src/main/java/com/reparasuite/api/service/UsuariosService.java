package com.reparasuite.api.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.UsuarioCrearRequest;
import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.dto.UsuarioUpdateRequest;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.RolUsuario;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class UsuariosService {

  private static final String SUBJECT_TYPE_BACKOFFICE = "BACKOFFICE";

  private final UsuarioRepo usuarioRepo;
  private final PasswordEncoder encoder;
  private final RefreshTokenService refreshTokenService;

  public UsuariosService(
      UsuarioRepo usuarioRepo,
      PasswordEncoder encoder,
      RefreshTokenService refreshTokenService
  ) {
    this.usuarioRepo = usuarioRepo;
    this.encoder = encoder;
    this.refreshTokenService = refreshTokenService;
  }

  public List<UsuarioResumenDto> listar(boolean activos) {
    return usuarioRepo.findAll().stream()
        .filter(u -> !activos || u.isActivo())
        .map(this::toDto)
        .toList();
  }

  public UsuarioResumenDto obtener(UUID id) {
    return toDto(usuarioRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado")));
  }

  @Transactional
  public UsuarioResumenDto crear(UsuarioCrearRequest req) {
    String usuarioNorm = normalizeRequired(req.usuario(), "usuario");
    String emailNorm = normalizeEmail(req.email());
    RolUsuario rol = parseRol(req.rol());

    if (usuarioRepo.existsByUsuarioIgnoreCase(usuarioNorm)) {
      throw new ConflictException("Ya existe un usuario con ese nombre de usuario");
    }

    if (usuarioRepo.existsByEmailIgnoreCase(emailNorm)) {
      throw new ConflictException("Ya existe un usuario con ese email");
    }

    Usuario u = new Usuario();
    u.setNombre(normalizeRequired(req.nombre(), "nombre"));
    u.setUsuario(usuarioNorm);
    u.setEmail(emailNorm);
    u.setRol(rol);
    u.setActivo(true);
    u.setPasswordHash(encoder.encode(normalizeRequired(req.password(), "password")));

    u = usuarioRepo.save(u);
    return toDto(u);
  }

  @Transactional
  public UsuarioResumenDto actualizar(UUID id, UsuarioUpdateRequest req) {
    Usuario u = usuarioRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

    String usuarioNorm = normalizeRequired(req.usuario(), "usuario");
    String emailNorm = normalizeEmail(req.email());
    RolUsuario rol = parseRol(req.rol());

    usuarioRepo.findByUsuarioIgnoreCase(usuarioNorm).ifPresent(existing -> {
      if (!existing.getId().equals(id)) {
        throw new ConflictException("Ya existe un usuario con ese nombre de usuario");
      }
    });

    usuarioRepo.findByEmailIgnoreCase(emailNorm).ifPresent(existing -> {
      if (!existing.getId().equals(id)) {
        throw new ConflictException("Ya existe un usuario con ese email");
      }
    });

    u.setNombre(normalizeRequired(req.nombre(), "nombre"));
    u.setUsuario(usuarioNorm);
    u.setEmail(emailNorm);
    u.setRol(rol);

    u = usuarioRepo.save(u);
    return toDto(u);
  }

  @Transactional
  public void cambiarEstado(UUID id, boolean activo) {
    Usuario u = usuarioRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    u.setActivo(activo);
    usuarioRepo.save(u);
  }

  @Transactional
  public void resetPassword(UUID id, String rawPassword) {
    Usuario u = usuarioRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

    String password = normalizeRequired(rawPassword, "password");
    if (password.length() < 8) {
      throw new BadRequestException("La contraseña debe tener al menos 8 caracteres");
    }

    u.setPasswordHash(encoder.encode(password));
    usuarioRepo.save(u);

    refreshTokenService.revokeAllBySubject(u.getId(), SUBJECT_TYPE_BACKOFFICE);
  }

  @Transactional
  public void eliminar(UUID id) {
    if (!usuarioRepo.existsById(id)) {
      throw new NotFoundException("Usuario no encontrado");
    }
    refreshTokenService.revokeAllBySubject(id, SUBJECT_TYPE_BACKOFFICE);
    usuarioRepo.deleteById(id);
  }

  private RolUsuario parseRol(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("El rol es obligatorio");
    }

    try {
      return RolUsuario.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Rol inválido: " + raw);
    }
  }

  private String normalizeRequired(String value, String field) {
    if (value == null || value.trim().isBlank()) {
      throw new BadRequestException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private String normalizeEmail(String email) {
    if (email == null || email.trim().isBlank()) {
      throw new BadRequestException("El email es obligatorio");
    }
    return email.trim().toLowerCase(Locale.ROOT);
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