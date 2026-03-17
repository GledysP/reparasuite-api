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
import com.reparasuite.api.model.FotoOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PagoOt;
import com.reparasuite.api.model.TicketFoto;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.repo.FotoOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.TicketFotoRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;
import com.reparasuite.api.service.AuthContextService;
import com.reparasuite.api.service.SecureUploadService;

@RestController
public class ArchivoController {

  private final SecureUploadService secureUploadService;
  private final OrdenTrabajoRepo ordenTrabajoRepo;
  private final TicketSolicitudRepo ticketSolicitudRepo;
  private final FotoOtRepo fotoOtRepo;
  private final PagoOtRepo pagoOtRepo;
  private final TicketFotoRepo ticketFotoRepo;
  private final AuthContextService authContextService;

  public ArchivoController(
      SecureUploadService secureUploadService,
      OrdenTrabajoRepo ordenTrabajoRepo,
      TicketSolicitudRepo ticketSolicitudRepo,
      FotoOtRepo fotoOtRepo,
      PagoOtRepo pagoOtRepo,
      TicketFotoRepo ticketFotoRepo,
      AuthContextService authContextService
  ) {
    this.secureUploadService = secureUploadService;
    this.ordenTrabajoRepo = ordenTrabajoRepo;
    this.ticketSolicitudRepo = ticketSolicitudRepo;
    this.fotoOtRepo = fotoOtRepo;
    this.pagoOtRepo = pagoOtRepo;
    this.ticketFotoRepo = ticketFotoRepo;
    this.authContextService = authContextService;
  }

  @GetMapping("/api/v1/archivos/ot/{otId}/{filename:.+}")
  public ResponseEntity<Resource> descargarOtImagen(
      @PathVariable UUID otId,
      @PathVariable String filename
  ) {
    OrdenTrabajo ot = requireAuthorizedOt(otId);

    FotoOt foto = fotoOtRepo.findByUrl("/api/v1/archivos/ot/" + otId + "/" + filename)
        .orElseThrow(() -> new NotFoundException("Archivo no encontrado"));

    var auth = authContextService.current();
    if (auth.isCliente() && !foto.isVisibleCliente()) {
      throw new ForbiddenException("No autorizado");
    }

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

    PagoOt pago = pagoOtRepo.findByComprobanteUrl("/api/v1/archivos/pagos/" + otId + "/" + filename)
        .orElseThrow(() -> new NotFoundException("Archivo no encontrado"));

    if (!pago.getOt().getId().equals(ot.getId())) {
      throw new ForbiddenException("No autorizado");
    }

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

    TicketFoto foto = ticketFotoRepo.findByUrl("/api/v1/archivos/tickets/" + ticketId + "/" + filename)
        .orElseThrow(() -> new NotFoundException("Archivo no encontrado"));

    if (!foto.getTicket().getId().equals(ticket.getId())) {
      throw new ForbiddenException("No autorizado");
    }

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