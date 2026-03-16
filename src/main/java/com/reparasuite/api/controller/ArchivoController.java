package com.reparasuite.api.controller;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;
import com.reparasuite.api.service.AuthContextService;
import com.reparasuite.api.service.SecureUploadService;

@RestController
public class ArchivoController {

  private final SecureUploadService secureUploadService;
  private final OrdenTrabajoRepo ordenTrabajoRepo;
  private final TicketSolicitudRepo ticketSolicitudRepo;
  private final AuthContextService authContextService;

  public ArchivoController(
      SecureUploadService secureUploadService,
      OrdenTrabajoRepo ordenTrabajoRepo,
      TicketSolicitudRepo ticketSolicitudRepo,
      AuthContextService authContextService
  ) {
    this.secureUploadService = secureUploadService;
    this.ordenTrabajoRepo = ordenTrabajoRepo;
    this.ticketSolicitudRepo = ticketSolicitudRepo;
    this.authContextService = authContextService;
  }

  @GetMapping("/api/v1/archivos/ot/{otId}/{filename:.+}")
  public ResponseEntity<Resource> descargarOtImagen(
      @PathVariable UUID otId,
      @PathVariable String filename
  ) {
    OrdenTrabajo ot = requireAuthorizedOt(otId);

    Resource resource = secureUploadService.loadOtImage(ot.getId(), filename);
    String contentType = secureUploadService.probeContentType(resource, filename);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(filename).build().toString()
        )
        .body(resource);
  }

  @GetMapping("/api/v1/archivos/pagos/{otId}/{filename:.+}")
  public ResponseEntity<Resource> descargarPagoComprobante(
      @PathVariable UUID otId,
      @PathVariable String filename
  ) {
    OrdenTrabajo ot = requireAuthorizedOt(otId);

    Resource resource = secureUploadService.loadPaymentReceipt(ot.getId(), filename);
    String contentType = secureUploadService.probeContentType(resource, filename);

    ContentDisposition disposition =
        "application/pdf".equalsIgnoreCase(contentType)
            ? ContentDisposition.attachment().filename(filename).build()
            : ContentDisposition.inline().filename(filename).build();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .body(resource);
  }

  @GetMapping("/api/v1/archivos/tickets/{ticketId}/{filename:.+}")
  public ResponseEntity<Resource> descargarTicketImagen(
      @PathVariable UUID ticketId,
      @PathVariable String filename
  ) {
    TicketSolicitud ticket = requireAuthorizedTicket(ticketId);

    Resource resource = secureUploadService.loadTicketImage(ticket.getId(), filename);
    String contentType = secureUploadService.probeContentType(resource, filename);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(filename).build().toString()
        )
        .body(resource);
  }

  private OrdenTrabajo requireAuthorizedOt(UUID otId) {
    OrdenTrabajo ot = ordenTrabajoRepo.findById(otId)
        .orElseThrow(() -> new NotFoundException("Orden no encontrada"));

    var auth = authContextService.current();

    if (auth.isBackoffice()) {
      return ot;
    }

    if (auth.isCliente() && ot.getCliente() != null && ot.getCliente().getId().equals(auth.id())) {
      return ot;
    }

    throw new ForbiddenException("No autorizado");
  }

  private TicketSolicitud requireAuthorizedTicket(UUID ticketId) {
    TicketSolicitud ticket = ticketSolicitudRepo.findById(ticketId)
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    var auth = authContextService.current();

    if (auth.isBackoffice()) {
      return ticket;
    }

    if (auth.isCliente() && ticket.getCliente() != null && ticket.getCliente().getId().equals(auth.id())) {
      return ticket;
    }

    throw new ForbiddenException("No autorizado");
  }
}