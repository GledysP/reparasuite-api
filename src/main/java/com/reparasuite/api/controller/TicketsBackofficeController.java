package com.reparasuite.api.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.TicketsService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/backoffice/tickets")
@Validated
public class TicketsBackofficeController {

  private final TicketsService ticketsService;

  public TicketsBackofficeController(TicketsService ticketsService) {
    this.ticketsService = ticketsService;
  }

  @GetMapping
  public ApiListaResponse<TicketBackofficeListaItemDto> listar(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size
  ) {
    return ticketsService.listarBackoffice(page, size);
  }

  @GetMapping("/{id}")
  public TicketDetalleDto obtener(@PathVariable String id) {
    return ticketsService.obtenerBackoffice(id);
  }

  @PostMapping("/{id}/mensajes")
  public void anadirMensaje(
      @PathVariable String id,
      @Valid @RequestBody MensajeEnviarRequest req
  ) {
    ticketsService.anadirMensajeBackoffice(id, req);
  }

  @PostMapping(
      value = "/{id}/fotos",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public TicketFotoDto subirFoto(
      @PathVariable String id,
      @RequestPart("file") MultipartFile file
  ) throws IOException {
    return ticketsService.subirFotoBackoffice(id, file);
  }

  /**
   * Crea OT desde ticket y la vincula automáticamente.
   * Idempotente: si el ticket ya tiene OT, devuelve la existente.
   */
  @PostMapping("/{id}/crear-ot")
  public TicketCrearOtResponse crearOtDesdeTicket(@PathVariable String id) {
    return ticketsService.crearOtDesdeTicketBackoffice(id);
  }
}