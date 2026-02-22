package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.TicketsService;

@RestController
@RequestMapping("/api/v1/backoffice/tickets")
public class TicketsBackofficeController {

  private final TicketsService service;

  public TicketsBackofficeController(TicketsService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<TicketBackofficeListaItemDto> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listarBackoffice(page, size);
  }

  @GetMapping("/{id}")
  public TicketDetalleDto obtener(@PathVariable String id) {
    return service.obtenerBackoffice(id);
  }

  @PostMapping("/{id}/mensajes")
  public ResponseEntity<?> anadirMensaje(@PathVariable String id, @Validated @RequestBody MensajeEnviarRequest req) {
    service.anadirMensajeBackoffice(id, req);
    return ResponseEntity.noContent().build();
  }

  // ✅ NUEVO: crear OT desde ticket
  @PostMapping("/{id}/crear-ot")
  public ResponseEntity<TicketCrearOtResponse> crearOtDesdeTicket(@PathVariable String id) {
    return ResponseEntity.ok(service.crearOtDesdeTicketBackoffice(id));
  }
}
