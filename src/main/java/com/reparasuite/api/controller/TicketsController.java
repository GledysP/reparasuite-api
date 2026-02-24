package com.reparasuite.api.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.TicketsService;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketsController {

  private final TicketsService service;

  public TicketsController(TicketsService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<TicketListaItemDto> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(page, size);
  }

  @GetMapping("/{id}")
  public TicketDetalleDto obtener(@PathVariable String id) {
    return service.obtener(id);
  }

  @PostMapping
  public ResponseEntity<TicketDetalleDto> crear(@Validated @RequestBody TicketCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  // ✅ NUEVO: subir foto al ticket (portal cliente)
  @PostMapping("/{id}/fotos")
  public ResponseEntity<TicketFotoDto> subirFoto(
      @PathVariable String id,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    return ResponseEntity.ok(service.subirFotoCliente(id, file));
  }

  @PostMapping("/{id}/mensajes")
  public ResponseEntity<?> anadirMensaje(@PathVariable String id, @Validated @RequestBody MensajeEnviarRequest req) {
    service.anadirMensaje(id, req);
    return ResponseEntity.noContent().build();
  }
}