package com.reparasuite.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.InventarioService;

@RestController
@RequestMapping("/api/v1/inventario")
@PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
public class InventarioController {

  private final InventarioService service;

  public InventarioController(InventarioService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<InventarioItemResumenDto> listar(
      @RequestParam(required = false) Boolean activo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(activo, page, size);
  }

  @GetMapping("/{id}")
  public InventarioItemDetalleDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  @PostMapping
  public ResponseEntity<InventarioItemDetalleDto> crear(@Validated @RequestBody InventarioItemCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<InventarioItemDetalleDto> actualizar(
      @PathVariable String id,
      @Validated @RequestBody InventarioItemCrearRequest req
  ) {
    return ResponseEntity.ok(service.actualizar(UUID.fromString(id), req));
  }

  @GetMapping("/catalogos/categorias")
  public List<InventarioCategoriaDto> categorias() {
    return service.categorias();
  }

  @GetMapping("/{id}/movimientos")
  public List<InventarioMovimientoDto> movimientos(@PathVariable String id) {
    return service.movimientos(UUID.fromString(id));
  }

  @PostMapping("/{id}/movimientos")
  public ResponseEntity<InventarioMovimientoDto> registrarMovimiento(
      @PathVariable String id,
      @Validated @RequestBody InventarioMovimientoCrearRequest req
  ) {
    return ResponseEntity.ok(service.registrarMovimiento(UUID.fromString(id), req));
  }
}