package com.reparasuite.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.model.RolUsuario;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class UsuariosService {

  private final UsuarioRepo usuarioRepo;
  private final PasswordEncoder encoder;

  public UsuariosService(UsuarioRepo usuarioRepo, PasswordEncoder encoder) {
    this.usuarioRepo = usuarioRepo;
    this.encoder = encoder;
  }

  public List<UsuarioResumenDto> listarActivos() {
    return usuarioRepo.findAll().stream()
        .filter(Usuario::isActivo)
        .map(this::toResumen)
        .toList();
  }

  public UsuarioDetalleDto obtener(UUID id) {
    Usuario u = usuarioRepo.findById(id).orElseThrow();
    return toDetalle(u);
  }

  public UsuarioDetalleDto crear(UsuarioCrearRequest req) {
    // En este MVP, usuario = email (para no pedir campo adicional).
    // Si quieres otro comportamiento, lo cambiamos después.
    Usuario u = new Usuario();
    u.setNombre(req.nombre());
    u.setEmail(req.email());
    u.setUsuario(req.email());
    u.setRol(RolUsuario.valueOf(req.rol()));
    u.setActivo(true);
    u.setPasswordHash(encoder.encode(req.password()));

    u = usuarioRepo.save(u);
    return toDetalle(u);
  }

  public UsuarioDetalleDto actualizar(UUID id, UsuarioUpdateRequest req) {
    Usuario u = usuarioRepo.findById(id).orElseThrow();

    u.setNombre(req.nombre());
    u.setEmail(req.email());
    u.setRol(RolUsuario.valueOf(req.rol()));

    // mantener coherencia: usuario = email
    u.setUsuario(req.email());

    if (req.password() != null && !req.password().isBlank()) {
      u.setPasswordHash(encoder.encode(req.password()));
    }

    u = usuarioRepo.save(u);
    return toDetalle(u);
  }

  public void cambiarEstado(UUID id, boolean activo) {
    Usuario u = usuarioRepo.findById(id).orElseThrow();
    u.setActivo(activo);
    usuarioRepo.save(u);
  }

  private UsuarioResumenDto toResumen(Usuario u) {
    return new UsuarioResumenDto(
        u.getId(),
        u.getNombre(),
        u.getUsuario(),
        u.getEmail(),
        u.getRol().name(),
        u.isActivo()
    );
  }

  private UsuarioDetalleDto toDetalle(Usuario u) {
    return new UsuarioDetalleDto(
        u.getId(),
        u.getNombre(),
        u.getUsuario(),
        u.getEmail(),
        u.getRol().name(),
        u.isActivo()
    );
  }
}
