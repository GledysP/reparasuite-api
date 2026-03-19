package com.reparasuite.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.CategoriaEquipoDto;
import com.reparasuite.api.dto.CategoriaEquipoFallaDto;
import com.reparasuite.api.dto.CategoriaEquipoGuardarRequest;
import com.reparasuite.api.dto.EquipoCrearRequest;
import com.reparasuite.api.dto.EquipoDetalleDto;
import com.reparasuite.api.dto.EquipoResumenDto;
import com.reparasuite.api.service.EquiposService;

@RestController
@RequestMapping("/api/v1/equipos")
@PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
public class EquiposController {

  private final EquiposService service;

  public EquiposController(EquiposService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<EquipoResumenDto> listar(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String clienteId,
      @RequestParam(required = false) Boolean activo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(query, clienteId, activo, page, size);
  }

  @GetMapping("/{id}")
  public EquipoDetalleDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  @PostMapping
  public ResponseEntity<EquipoDetalleDto> crear(@Validated @RequestBody EquipoCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<EquipoDetalleDto> actualizar(
      @PathVariable String id,
      @Validated @RequestBody EquipoCrearRequest req
  ) {
    return ResponseEntity.ok(service.actualizar(UUID.fromString(id), req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> desactivar(@PathVariable String id) {
    service.desactivar(UUID.fromString(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/catalogos/categorias")
  public List<CategoriaEquipoDto> categorias() {
    return service.categorias();
  }

  @PostMapping("/catalogos/categorias")
  public ResponseEntity<CategoriaEquipoDto> crearCategoria(@Validated @RequestBody CategoriaEquipoGuardarRequest req) {
    return ResponseEntity.ok(service.crearCategoria(req));
  }

  @PutMapping("/catalogos/categorias/{id}")
  public ResponseEntity<CategoriaEquipoDto> actualizarCategoria(
      @PathVariable String id,
      @Validated @RequestBody CategoriaEquipoGuardarRequest req
  ) {
    return ResponseEntity.ok(service.actualizarCategoria(UUID.fromString(id), req));
  }

  @GetMapping("/catalogos/categorias/{categoriaId}/fallas")
  public List<CategoriaEquipoFallaDto> fallas(@PathVariable String categoriaId) {
    return service.fallasPorCategoria(UUID.fromString(categoriaId));
  }
}