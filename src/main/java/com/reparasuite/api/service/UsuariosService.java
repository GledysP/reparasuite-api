package com.reparasuite.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class UsuariosService {

  private final UsuarioRepo usuarioRepo;

  public UsuariosService(UsuarioRepo usuarioRepo) {
    this.usuarioRepo = usuarioRepo;
  }

  public List<UsuarioResumenDto> listarActivos() {
    return usuarioRepo.findAll().stream()
        .filter(u -> u.isActivo())
        .map(u -> new UsuarioResumenDto(u.getId(), u.getNombre(), u.getUsuario(), u.getRol().name(), u.isActivo()))
        .toList();
  }
}
