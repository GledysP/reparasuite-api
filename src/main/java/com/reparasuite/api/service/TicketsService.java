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
public class TicketsService {

  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo msgRepo;
  private final ClienteRepo clienteRepo;

  public TicketsService(TicketSolicitudRepo ticketRepo, TicketMensajeRepo msgRepo, ClienteRepo clienteRepo) {
    this.ticketRepo = ticketRepo;
    this.msgRepo = msgRepo;
    this.clienteRepo = clienteRepo;
  }

  public ApiListaResponse<TicketListaItemDto> listar(int page, int size) {
    UUID clienteId = clienteId();
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<TicketSolicitud> p = ticketRepo.findByCliente_Id(clienteId, pageable);

    List<TicketListaItemDto> items = p.getContent().stream()
        .map(t -> new TicketListaItemDto(t.getId(), t.getEstado().name(), t.getAsunto(), t.getUpdatedAt()))
        .toList();

    return new ApiListaResponse<>(items, p.getTotalElements());
  }

  public TicketDetalleDto obtener(String id) {
    UUID clienteId = clienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(id)).orElseThrow();
    if (!t.getCliente().getId().equals(clienteId)) throw new RuntimeException("No autorizado");

    List<MensajeDto> mensajes = msgRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId()).stream()
        .map(m -> new MensajeDto(m.getId(), m.getRemitenteTipo().name(), m.getRemitenteNombre(), m.getContenido(), m.getCreatedAt()))
        .toList();

    return new TicketDetalleDto(t.getId(), t.getEstado().name(), t.getAsunto(), t.getDescripcion(), mensajes, t.getCreatedAt(), t.getUpdatedAt());
  }

  @Transactional
  public TicketDetalleDto crear(TicketCrearRequest req) {
    UUID clienteId = clienteId();
    Cliente c = clienteRepo.findById(clienteId).orElseThrow();

    TicketSolicitud t = new TicketSolicitud();
    t.setCliente(c);
    t.setEstado(EstadoTicket.ABIERTO);
    t.setAsunto(req.asunto());
    t.setDescripcion(req.descripcion());
    t = ticketRepo.save(t);

    // mensaje inicial
    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.CLIENTE);
    m.setRemitenteNombre(c.getNombre());
    m.setContenido("Solicitud creada: " + recortar(req.descripcion(), 500));
    msgRepo.save(m);

    return obtener(t.getId().toString());
  }

  @Transactional
  public void anadirMensaje(String ticketId, MensajeEnviarRequest req) {
    UUID clienteId = clienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();
    if (!t.getCliente().getId().equals(clienteId)) throw new RuntimeException("No autorizado");

    Cliente c = clienteRepo.findById(clienteId).orElseThrow();

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.CLIENTE);
    m.setRemitenteNombre(c.getNombre());
    m.setContenido(req.contenido());
    msgRepo.save(m);

    // touch updatedAt
    t.setEstado(t.getEstado());
    ticketRepo.save(t);
  }

  private UUID clienteId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      String rol = String.valueOf(jwtAuth.getToken().getClaims().get("rol"));
      if (!"CLIENTE".equalsIgnoreCase(rol)) throw new RuntimeException("No autorizado");
      return UUID.fromString(jwtAuth.getToken().getSubject());
    }
    throw new RuntimeException("No autenticado");
  }

  private String recortar(String s, int max) {
    if (s == null) return "";
    String t = s.trim();
    if (t.length() <= max) return t;
    return t.substring(0, max - 1) + "…";
  }
}
