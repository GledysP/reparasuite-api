package com.reparasuite.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.model.*;
import com.reparasuite.api.repo.*;

@Service
public class TicketsService {

  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo msgRepo;
  private final TicketFotoRepo fotoRepo;
  private final ClienteRepo clienteRepo;
  private final OrdenesTrabajoService ordenesTrabajoService;
  private final OrdenTrabajoRepo otRepo;

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public TicketsService(
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo msgRepo,
      TicketFotoRepo fotoRepo,
      ClienteRepo clienteRepo,
      OrdenesTrabajoService ordenesTrabajoService,
      OrdenTrabajoRepo otRepo
  ) {
    this.ticketRepo = ticketRepo;
    this.msgRepo = msgRepo;
    this.fotoRepo = fotoRepo;
    this.clienteRepo = clienteRepo;
    this.ordenesTrabajoService = ordenesTrabajoService;
    this.otRepo = otRepo;
  }

  // =========================
  // PORTAL CLIENTE
  // =========================

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

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new RuntimeException("No autorizado");
    }

    return toDetalleDto(t);
  }

  @Transactional
  public TicketDetalleDto crear(TicketCrearRequest req) {
    UUID clienteId = clienteId();
    Cliente c = clienteRepo.findById(clienteId).orElseThrow();

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
    UUID clienteId = clienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new RuntimeException("No autorizado");
    }

    Cliente c = clienteRepo.findById(clienteId).orElseThrow();

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
    UUID clienteId = clienteId();
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();

    if (!t.getCliente().getId().equals(clienteId)) {
      throw new RuntimeException("No autorizado");
    }

    return guardarFotoTicket(t, file);
  }

  // =========================
  // BACKOFFICE
  // =========================

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
    TicketSolicitud t = ticketRepo.findById(UUID.fromString(id)).orElseThrow();
    return toDetalleDto(t);
  }

  @Transactional
  public void anadirMensajeBackoffice(String ticketId, MensajeEnviarRequest req) {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(nombreActorBackoffice());
    m.setContenido(req.contenido());
    msgRepo.save(m);

    ticketRepo.save(t);
  }

  @Transactional
  public TicketFotoDto subirFotoBackoffice(String ticketId, MultipartFile file) throws IOException {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();
    return guardarFotoTicket(t, file);
  }

  @Transactional
  public TicketCrearOtResponse crearOtDesdeTicketBackoffice(String ticketId) {
    requireBackofficeRole();

    TicketSolicitud t = ticketRepo.findById(UUID.fromString(ticketId)).orElseThrow();

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
    m.setRemitenteNombre(nombreActorBackoffice());
    m.setContenido("Se creó una OT desde este ticket: " + creada.codigo());
    msgRepo.save(m);

    return new TicketCrearOtResponse(t.getId(), creada.id(), creada.codigo());
  }

  // =========================
  // Mapeo DTO
  // =========================

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

  // =========================
  // Fotos ticket
  // =========================

  private TicketFotoDto guardarFotoTicket(TicketSolicitud ticket, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new RuntimeException("Archivo vacío");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
      throw new RuntimeException("Solo se permiten imágenes");
    }

    Path baseDir = Paths.get(uploadDir).resolve("tickets").resolve(ticket.getId().toString());
    Files.createDirectories(baseDir);

    String original = safeName(file.getOriginalFilename());
    String filename = UUID.randomUUID() + "-" + original;

    Path path = baseDir.resolve(filename);
    Files.write(path, file.getBytes(), StandardOpenOption.CREATE_NEW);

    String url = "/files/tickets/" + ticket.getId() + "/" + filename;

    TicketFoto foto = new TicketFoto();
    foto.setTicket(ticket);
    foto.setUrl(url);
    foto.setNombreOriginal(file.getOriginalFilename());
    foto = fotoRepo.save(foto);

    ticketRepo.save(ticket);

    return new TicketFotoDto(foto.getId(), foto.getUrl(), foto.getNombreOriginal(), foto.getCreatedAt());
  }

  // =========================
  // Seguridad
  // =========================

  private UUID clienteId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      String rol = String.valueOf(jwtAuth.getToken().getClaims().get("rol"));
      if (!"CLIENTE".equalsIgnoreCase(rol)) throw new RuntimeException("No autorizado");
      return UUID.fromString(jwtAuth.getToken().getSubject());
    }
    throw new RuntimeException("No autenticado");
  }

  private void requireBackofficeRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      String rol = String.valueOf(jwtAuth.getToken().getClaims().get("rol"));
      if (!"ADMIN".equalsIgnoreCase(rol) && !"TECNICO".equalsIgnoreCase(rol)) {
        throw new RuntimeException("No autorizado");
      }
      return;
    }
    throw new RuntimeException("No autenticado");
  }

  private String nombreActorBackoffice() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Object n = jwtAuth.getToken().getClaims().get("nombre");
      if (n != null) return String.valueOf(n);
      return "Backoffice";
    }
    return "Backoffice";
  }

  // =========================
  // Helpers dominio/texto
  // =========================

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

  private String safeName(String n) {
    if (n == null || n.isBlank()) return "imagen.jpg";
    return n.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}