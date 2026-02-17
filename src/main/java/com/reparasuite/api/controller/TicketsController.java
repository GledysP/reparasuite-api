package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping("/{id}/mensajes")
  public ResponseEntity<?> anadirMensaje(@PathVariable String id, @Validated @RequestBody MensajeEnviarRequest req) {
    service.anadirMensaje(id, req);
    return ResponseEntity.noContent().build();
  }
}
