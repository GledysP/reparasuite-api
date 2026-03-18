package com.reparasuite.api.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.MensajeDto;
import com.reparasuite.api.dto.MensajeEnviarRequest;
import com.reparasuite.api.dto.TicketBackofficeListaItemDto;
import com.reparasuite.api.dto.TicketCrearOtResponse;
import com.reparasuite.api.dto.TicketCrearRequest;
import com.reparasuite.api.dto.TicketDetalleDto;
import com.reparasuite.api.dto.TicketFotoDto;
import com.reparasuite.api.dto.TicketListaItemDto;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoTicket;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.TicketFoto;
import com.reparasuite.api.model.TicketMensaje;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.model.TipoRemitente;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TicketFotoRepo;
import com.reparasuite.api.repo.TicketMensajeRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;

@Service
public class TicketsService {

  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo msgRepo;
  private final TicketFotoRepo fotoRepo;
  private final ClienteRepo clienteRepo;
  private final OrdenesTrabajoService ordenesTrabajoService;
  private final OrdenTrabajoRepo otRepo;
  private final AuthContextService authContextService;
  private final SecureUploadService secureUploadService;

  public TicketsService(
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo msgRepo,
      TicketFotoRepo fotoRepo,
      ClienteRepo clienteRepo,
      OrdenesTrabajoService ordenesTrabajoService,
      OrdenTrabajoRepo otRepo,
      AuthContextService authContextService,
      SecureUploadService secureUploadService
  ) {
    this.ticketRepo = ticketRepo;
    this.msgRepo = msgRepo;
    this.fotoRepo = fotoRepo;
    this.clienteRepo = clienteRepo;
    this.ordenesTrabajoService = ordenesTrabajoService;
    this.otRepo = otRepo;
    this.authContextService = authContextService;
    this.secureUploadService = secureUploadService;
  }

  public ApiListaResponse<TicketListaItemDto> listar(int page, int size) {
    UUID clienteId = requireClienteId();
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<TicketSolicitud> p = ticketRepo.findByCliente_Id(clienteId, pageable);

    List<TicketListaItemDto> items = p.getContent().stream()
        .map(t -> new TicketListaItemDto(t.getId(), t.getEstado().name(), t.getAsunto(), t.getUpdatedAt()))
        .toList();

    return new ApiListaResponse<>(items, p.getTotalElements());
  }

  public TicketDetalleDto obtener(String id) {
    UUID clienteId = requireClienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(id))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new ForbiddenException("No autorizado");
    }

    return toDetalleDto(t);
  }

  @Transactional
  public TicketDetalleDto crear(TicketCrearRequest req) {
    UUID clienteId = requireClienteId();
    Cliente c = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

    TicketSolicitud t = new TicketSolicitud();
    t.setCliente(c);
    t.setEstado(EstadoTicket.ABIERTO);

    String equipoAsunto = limpio(req.asunto());
    String descripcionLegacy = limpio(req.descripcion());

    String equipo = limpioNullable(req.equipo());
    String falla = limpioNullable(req.descripcionFalla());
    String tipoSug = normalizarTipoServicio(req.tipoServicioSugerido());
    String direccion = limpioNullable(req.direccion());
    String observaciones = limpioNullable(req.observaciones());

    if (!notBlank(equipo) && notBlank(equipoAsunto)) {
      equipo = equipoAsunto;
    }

    t.setAsunto(equipoAsunto);
    t.setEquipo(equipo);
    t.setDescripcionFalla(falla);
    t.setTipoServicioSugerido(tipoSug);
    t.setDireccion(direccion);
    t.setObservaciones(observaciones);

    t.setClienteNombreSnapshot(c.getNombre());
    t.setClienteTelefonoSnapshot(c.getTelefono());
    t.setClienteEmailSnapshot(c.getEmail());

    String descripcionFinal = construirDescripcionTicket(
        descripcionLegacy, equipo, falla, tipoSug, direccion, observaciones
    );
    t.setDescripcion(descripcionFinal);

    t = ticketRepo.save(t);

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.CLIENTE);
    m.setRemitenteNombre(c.getNombre());
    m.setContenido("Solicitud creada");
    msgRepo.save(m);

    return toDetalleDto(t);
  }

  @Transactional
  public void anadirMensaje(String ticketId, MensajeEnviarRequest req) {
    UUID clienteId = requireClienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new ForbiddenException("No autorizado");
    }

    Cliente c = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.CLIENTE);
    m.setRemitenteNombre(c.getNombre());
    m.setContenido(req.contenido());
    msgRepo.save(m);

    ticketRepo.save(t);
  }

  @Transactional
  public TicketFotoDto subirFotoCliente(String ticketId, MultipartFile file) throws IOException {
    UUID clienteId = requireClienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new ForbiddenException("No autorizado");
    }

    return guardarFotoTicket(t, file);
  }

  public ApiListaResponse<TicketBackofficeListaItemDto> listarBackoffice(int page, int size) {
    requireBackofficeRole();

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<TicketSolicitud> p = ticketRepo.findAll(pageable);

    List<TicketBackofficeListaItemDto> items = p.getContent().stream()
        .map(t -> new TicketBackofficeListaItemDto(
            t.getId(),
            t.getEstado().name(),
            t.getAsunto(),
            t.getUpdatedAt(),
            t.getCliente().getId(),
            coalesce(t.getClienteNombreSnapshot(), t.getCliente().getNombre()),
            coalesce(t.getClienteEmailSnapshot(), t.getCliente().getEmail()),
            t.getOrdenTrabajoId()
        ))
        .toList();

    return new ApiListaResponse<>(items, p.getTotalElements());
  }

  public TicketDetalleDto obtenerBackoffice(String id) {
    requireBackofficeRole();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(id))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));
    return toDetalleDto(t);
  }

  @Transactional
  public void anadirMensajeBackoffice(String ticketId, MensajeEnviarRequest req) {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(auth().displayName());
    m.setContenido(req.contenido());
    msgRepo.save(m);

    ticketRepo.save(t);
  }

  @Transactional
  public TicketFotoDto subirFotoBackoffice(String ticketId, MultipartFile file) throws IOException {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));
    return guardarFotoTicket(t, file);
  }

  @Transactional
  public TicketCrearOtResponse crearOtDesdeTicketBackoffice(String ticketId) {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    UUID otExistenteId = t.getOrdenTrabajoId();
    if (otExistenteId != null) {
      String codigoExistente = otRepo.findById(otExistenteId)
          .map(OrdenTrabajo::getCodigo)
          .orElse(null);

      return new TicketCrearOtResponse(t.getId(), otExistenteId, codigoExistente);
    }

    var creada = ordenesTrabajoService.crearDesdeTicket(t);

    t.setOrdenTrabajoId(creada.id());

    if (t.getEstado() == EstadoTicket.ABIERTO) {
      t.setEstado(EstadoTicket.EN_REVISION);
    }
    ticketRepo.save(t);

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(auth().displayName());
    m.setContenido("Se creó una OT desde este ticket: " + creada.codigo());
    msgRepo.save(m);

    return new TicketCrearOtResponse(t.getId(), creada.id(), creada.codigo());
  }

  private TicketDetalleDto toDetalleDto(TicketSolicitud t) {
    List<MensajeDto> mensajes = msgRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId()).stream()
        .map(m -> new MensajeDto(
            m.getId(),
            m.getRemitenteTipo().name(),
            m.getRemitenteNombre(),
            m.getContenido(),
            m.getCreatedAt()
        ))
        .toList();

    List<TicketFotoDto> fotos = fotoRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId()).stream()
        .map(f -> new TicketFotoDto(
            f.getId(),
            f.getUrl(),
            f.getNombreOriginal(),
            f.getCreatedAt()
        ))
        .toList();

    return new TicketDetalleDto(
        t.getId(),
        t.getEstado().name(),
        t.getAsunto(),
        t.getDescripcion(),
        mensajes,
        t.getCreatedAt(),
        t.getUpdatedAt(),
        t.getOrdenTrabajoId(),
        t.getClienteNombreSnapshot(),
        t.getClienteTelefonoSnapshot(),
        t.getClienteEmailSnapshot(),
        t.getEquipo(),
        t.getDescripcionFalla(),
        t.getTipoServicioSugerido(),
        t.getDireccion(),
        t.getObservaciones(),
        fotos
    );
  }

  private TicketFotoDto guardarFotoTicket(TicketSolicitud ticket, MultipartFile file) throws IOException {
    SecureUploadService.StoredFile stored = secureUploadService.storeTicketImage(ticket.getId(), file);

    TicketFoto foto = new TicketFoto();
    foto.setTicket(ticket);
    foto.setUrl(stored.url());
    foto.setNombreOriginal(stored.originalFilename());
    foto = fotoRepo.save(foto);

    ticketRepo.save(ticket);

    return new TicketFotoDto(
        foto.getId(),
        foto.getUrl(),
        foto.getNombreOriginal(),
        foto.getCreatedAt()
    );
  }

  private UUID requireClienteId() {
    var user = auth();
    if (!user.isCliente()) {
      throw new ForbiddenException("No autorizado");
    }
    return user.id();
  }

  private void requireBackofficeRole() {
    if (!auth().isBackoffice()) {
      throw new ForbiddenException("No autorizado");
    }
  }

  private AuthContextService.AuthUser auth() {
    return authContextService.current();
  }

  private String construirDescripcionTicket(
      String descripcionLegacy,
      String equipo,
      String falla,
      String tipoServicioSugerido,
      String direccion,
      String observaciones
  ) {
    boolean hayEstructurado =
        notBlank(falla) || notBlank(tipoServicioSugerido) || notBlank(direccion) || notBlank(observaciones);

    if (!hayEstructurado) {
      return notBlank(descripcionLegacy)
          ? descripcionLegacy
          : (notBlank(equipo) ? "Falla pendiente de detallar" : "");
    }

    StringBuilder sb = new StringBuilder();

    if (notBlank(falla)) {
      sb.append("Falla reportada: ").append(falla.trim()).append("\n");
    }

    if (notBlank(tipoServicioSugerido)) {
      sb.append("Tipo sugerido: ").append(tipoServicioSugerido.trim()).append("\n");
    }

    if (notBlank(direccion)) {
      sb.append("Dirección / ubicación: ").append(direccion.trim()).append("\n");
    }

    if (notBlank(observaciones)) {
      sb.append("Observaciones: ").append(observaciones.trim()).append("\n");
    }

    if (notBlank(descripcionLegacy)) {
      sb.append("\nDetalle adicional:\n").append(descripcionLegacy.trim());
    }

    return sb.toString().trim();
  }

  private String normalizarTipoServicio(String v) {
    if (v == null) return null;
    String x = v.trim().toUpperCase();
    if (x.isBlank()) return null;
    if (!x.equals("TIENDA") && !x.equals("DOMICILIO")) return null;
    return x;
  }

  private String limpio(String s) {
    if (s == null) return "";
    return s.trim();
  }

  private String limpioNullable(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isBlank() ? null : t;
  }

  private boolean notBlank(String s) {
    return s != null && !s.trim().isBlank();
  }

  private String coalesce(String a, String b) {
    return (a != null && !a.isBlank()) ? a : b;
  }
}