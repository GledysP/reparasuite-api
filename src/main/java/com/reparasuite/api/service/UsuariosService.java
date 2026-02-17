package com.reparasuite.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public List<UsuarioResumenDto> listar(boolean activos) {
    return usuarioRepo.findAll().stream()
        .filter(u -> !activos || u.isActivo())
        .map(this::toDto)
        .toList();
  }

  public UsuarioResumenDto obtener(UUID id) {
    return toDto(usuarioRepo.findById(id).orElseThrow());
  }

  @Transactional
  public UsuarioResumenDto crear(UsuarioCrearRequest req) {
    Usuario u = new Usuario();
    u.setNombre(req.nombre());
    u.setUsuario(req.usuario());
    u.setEmail(req.email());
    u.setRol(RolUsuario.valueOf(req.rol()));
    u.setActivo(true);
    u.setPasswordHash(encoder.encode(req.password()));
    u = usuarioRepo.save(u);
    return toDto(u);
  }

  @Transactional
  public UsuarioResumenDto actualizar(UUID id, UsuarioUpdateRequest req) {
    Usuario u = usuarioRepo.findById(id).orElseThrow();
    u.setNombre(req.nombre());
    u.setUsuario(req.usuario());
    u.setEmail(req.email());
    u.setRol(RolUsuario.valueOf(req.rol()));
    u = usuarioRepo.save(u);
    return toDto(u);
  }

  @Transactional
  public void cambiarEstado(UUID id, boolean activo) {
    Usuario u = usuarioRepo.findById(id).orElseThrow();
    u.setActivo(activo);
    usuarioRepo.save(u);
  }

  @Transactional
  public void eliminar(UUID id) {
    usuarioRepo.deleteById(id); // ✅ delete físico
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
