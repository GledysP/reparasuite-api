package com.reparasuite.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.UsuariosService;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuariosController {

  private final UsuariosService service;

  public UsuariosController(UsuariosService service) {
    this.service = service;
  }

  @GetMapping
  public List<UsuarioResumenDto> listar(@RequestParam(defaultValue = "true") boolean activos) {
    return service.listar(activos);
  }

  @GetMapping("/{id}")
  public UsuarioResumenDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  @PostMapping
  public ResponseEntity<UsuarioResumenDto> crear(@Validated @RequestBody UsuarioCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UsuarioResumenDto> actualizar(@PathVariable String id, @Validated @RequestBody UsuarioUpdateRequest req) {
    return ResponseEntity.ok(service.actualizar(UUID.fromString(id), req));
  }

  @PatchMapping("/{id}/estado")
  public ResponseEntity<?> estado(@PathVariable String id, @Validated @RequestBody UsuarioEstadoRequest req) {
    service.cambiarEstado(UUID.fromString(id), req.activo());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> eliminar(@PathVariable String id) {
    service.eliminar(UUID.fromString(id));
    return ResponseEntity.noContent().build();
  }
}