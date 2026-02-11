package com.reparasuite.api.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.OrdenesTrabajoService;

@RestController
@RequestMapping("/api/v1/ordenes-trabajo")
public class OrdenesTrabajoController {

  private final OrdenesTrabajoService service;

  public OrdenesTrabajoController(OrdenesTrabajoService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<OtListaItemDto> listar(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(query, page, size);
  }

  @GetMapping("/{id}")
  public OtDetalleDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }

  @PostMapping
  public ResponseEntity<?> crear(@Validated @RequestBody OtCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @PatchMapping("/{id}/estado")
  public ResponseEntity<?> cambiarEstado(@PathVariable String id, @Validated @RequestBody OtCambiarEstadoRequest req) {
    service.cambiarEstado(UUID.fromString(id), req.estado());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/notas")
  public ResponseEntity<?> anadirNota(@PathVariable String id, @Validated @RequestBody OtNotaRequest req) {
    service.anadirNota(UUID.fromString(id), req.contenido());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/fotos")
  public ResponseEntity<FotoDto> subirFoto(@PathVariable String id, @RequestParam("file") MultipartFile file) throws IOException {
    return ResponseEntity.ok(service.subirFoto(UUID.fromString(id), file));
  }
}
