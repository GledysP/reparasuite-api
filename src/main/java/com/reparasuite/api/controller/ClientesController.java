package com.reparasuite.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.ClientesService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClientesController {

  private final ClientesService service;

  public ClientesController(ClientesService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<ClienteResumenDto> listar(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(query, page, size);
  }

  @GetMapping("/{id}")
  public ClienteResumenDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  @GetMapping("/{id}/ordenes-trabajo")
  public ApiListaResponse<ClienteOtItemDto> ordenesTrabajo(
      @PathVariable String id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.ordenesTrabajo(UUID.fromString(id), page, size);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> eliminar(@PathVariable String id) {
    service.eliminar(UUID.fromString(id));
    return ResponseEntity.noContent().build();
  }
}