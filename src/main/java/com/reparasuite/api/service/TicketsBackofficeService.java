package com.reparasuite.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.model.*;
import com.reparasuite.api.repo.*;

@Service
public class TicketsBackofficeService {

  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo msgRepo;

  public TicketsBackofficeService(TicketSolicitudRepo ticketRepo, TicketMensajeRepo msgRepo) {
    this.ticketRepo = ticketRepo;
    this.msgRepo = msgRepo;
  }

  public ApiListaResponse<TicketBackofficeListaItemDto> listar(int page, int size, String estado, String q) {
    requireStaff();

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<TicketSolicitud> p;

    boolean hasQ = q != null && !q.trim().isEmpty();
    boolean hasEstado = estado != null && !estado.trim().isEmpty();

    if (hasEstado) {
      EstadoTicket st = EstadoTicket.valueOf(estado.trim().toUpperCase());
      p = ticketRepo.findByEstado(st, pageable);
    } else if (hasQ) {
      String term = q.trim();
      p = ticketRepo.findByAsuntoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(term, term, pageable);
    } else {
      p = ticketRepo.findAll(pageable);
    }

    List<TicketBackofficeListaItemDto> items = p.getContent().stream().map(t ->
        new TicketBackofficeListaItemDto(
            t.getId(),
            t.getEstado().name(),
            t.getAsunto(),
            t.getUpdatedAt(),
            t.getCliente().getId(),
            t.getCliente().getNombre(),
            t.getCliente().getEmail()
        )
    ).toList();

    return new ApiListaResponse<>(items, p.getTotalElements());
  }

  public TicketDetalleDto obtener(String id) {
    requireStaff();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(id)).orElseThrow();

    List<MensajeDto> mensajes = msgRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId()).stream()
        .map(m -> new MensajeDto(m.getId(), m.getRemitenteTipo().name(), m.getRemitenteNombre(), m.getContenido(), m.getCreatedAt()))
        .toList();

    return new TicketDetalleDto(t.getId(), t.getEstado().name(), t.getAsunto(), t.getDescripcion(), mensajes, t.getCreatedAt(), t.getUpdatedAt());
  }

  @Transactional
  public void responder(String ticketId, MensajeEnviarRequest req) {
    requireStaff();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(staffName());
    m.setContenido(req.contenido());
    msgRepo.save(m);

    // touch updatedAt
    t.setEstado(t.getEstado());
    ticketRepo.save(t);
  }

  private void requireStaff() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      String rol = String.valueOf(jwtAuth.getToken().getClaims().get("rol"));
      if ("ADMIN".equalsIgnoreCase(rol) || "TECNICO".equalsIgnoreCase(rol) || "USUARIO".equalsIgnoreCase(rol)) return;
      throw new RuntimeException("No autorizado");
    }
    throw new RuntimeException("No autenticado");
  }

  private String staffName() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Object n = jwtAuth.getToken().getClaims().get("nombre");
      if (n != null) return String.valueOf(n);
      Object u = jwtAuth.getToken().getClaims().get("usuario");
      if (u != null) return String.valueOf(u);
    }
    return "BackOffice";
  }
}
