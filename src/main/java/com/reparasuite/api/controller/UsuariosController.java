package com.reparasuite.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.service.UsuariosService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosController {

  private final UsuariosService service;

  public UsuariosController(UsuariosService service) {
    this.service = service;
  }

  @GetMapping
  public List<UsuarioResumenDto> listar(@RequestParam(defaultValue = "true") boolean activos) {
    // MVP: solo activos
    return service.listarActivos();
  }
}
