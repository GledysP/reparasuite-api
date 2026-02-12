package com.reparasuite.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
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
    // MVP: solo activos (como ya tenías)
    return service.listarActivos();
  }

  // ✅ NUEVO: GET /api/v1/usuarios/{id}
  @GetMapping("/{id}")
  public UsuarioDetalleDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  // ✅ NUEVO: POST /api/v1/usuarios
  @PostMapping
  public ResponseEntity<UsuarioDetalleDto> crear(@Validated @RequestBody UsuarioCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  // ✅ NUEVO: PUT /api/v1/usuarios/{id}
  @PutMapping("/{id}")
  public ResponseEntity<UsuarioDetalleDto> actualizar(@PathVariable String id, @Validated @RequestBody UsuarioUpdateRequest req) {
    return ResponseEntity.ok(service.actualizar(UUID.fromString(id), req));
  }

  // ✅ NUEVO: PATCH /api/v1/usuarios/{id}/estado
  @PatchMapping("/{id}/estado")
  public ResponseEntity<?> cambiarEstado(@PathVariable String id, @Validated @RequestBody UsuarioEstadoRequest req) {
    service.cambiarEstado(UUID.fromString(id), req.activo());
    return ResponseEntity.noContent().build();
  }
}
