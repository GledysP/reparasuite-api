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

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public TicketsService(
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo msgRepo,
      TicketFotoRepo fotoRepo,
      ClienteRepo clienteRepo,
      OrdenesTrabajoService ordenesTrabajoService
  ) {
    this.ticketRepo = ticketRepo;
    this.msgRepo = msgRepo;
    this.fotoRepo = fotoRepo;
    this.clienteRepo = clienteRepo;
    this.ordenesTrabajoService = ordenesTrabajoService;
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

    // ✅ "asunto" se usa como EQUIPO (sin redundancia en UI)
    String equipoAsunto = limpio(req.asunto());

    // Compatibilidad legacy
    String descripcionLegacy = limpio(req.descripcion());

    // Campos estructurados (si existen en tu record)
    String equipo = limpioNullable(readEquipo(req)); // si no existe campo equipo, cae a null
    String falla = limpioNullable(readDescripcionFalla(req));
    String tipoSug = normalizarTipoServicio(readTipoServicioSugerido(req));
    String direccion = limpioNullable(readDireccion(req));

    // ✅ si no viene equipo explícito, usar asunto como equipo
    if (!notBlank(equipo) && notBlank(equipoAsunto)) {
      equipo = equipoAsunto;
    }

    // Campo principal visible en listas/backoffice (compatibilidad actual)
    t.setAsunto(equipoAsunto);

    // Estructurados (si tu entidad TicketSolicitud ya los tiene)
    safeSetEquipo(t, equipo);
    safeSetDescripcionFalla(t, falla);
    safeSetTipoServicioSugerido(t, tipoSug);
    safeSetDireccion(t, direccion);

    // snapshot del cliente
    safeSetClienteSnapshot(t, c);

    // descripcion legacy para compatibilidad, pero limpia (sin redundancia)
    String descripcionFinal = construirDescripcionTicket(descripcionLegacy, equipo, falla, tipoSug, direccion);
    t.setDescripcion(descripcionFinal);

    t = ticketRepo.save(t);

    // ✅ mensaje inicial más corto (recomendado)
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

    t.setEstado(t.getEstado()); // touch updatedAt
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
            coalesce(readClienteNombreSnapshot(t), t.getCliente().getNombre()),
            coalesce(readClienteEmailSnapshot(t), t.getCliente().getEmail())
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

    t.setEstado(t.getEstado()); // touch updatedAt
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

    // ✅ idempotente: si ya está ligada, devuelve la misma
    if (readOrdenTrabajoId(t) != null) {
      return new TicketCrearOtResponse(t.getId(), readOrdenTrabajoId(t), null);
    }

    var creada = ordenesTrabajoService.crearDesdeTicket(t);

    safeSetOrdenTrabajoId(t, creada.id());
    ticketRepo.save(t);

    TicketMensaje m = new TicketMensaje();
    m.setTicket(t);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(nombreActorBackoffice());
    m.setContenido("Se creó una OT desde este ticket: " + creada.codigo());
    msgRepo.save(m);

    if (t.getEstado() == EstadoTicket.ABIERTO) {
      t.setEstado(EstadoTicket.EN_REVISION);
      ticketRepo.save(t);
    }

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
        t.getAsunto(),             // ✅ asunto = equipo (UI simple)
        t.getDescripcion(),
        mensajes,
        t.getCreatedAt(),
        t.getUpdatedAt(),
        readOrdenTrabajoId(t),

        readClienteNombreSnapshot(t),
        readClienteTelefonoSnapshot(t),
        readClienteEmailSnapshot(t),

        readEquipoField(t),                 // equipo
        readDescripcionFallaField(t),       // descripcionFalla
        readTipoServicioSugeridoField(t),   // tipoServicioSugerido
        readDireccionField(t),              // direccion

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

    ticket.setEstado(ticket.getEstado()); // touch updatedAt
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
      String direccion
  ) {
    // ✅ Sin redundancia: NO repetir equipo si asunto ya lo representa
    boolean hayEstructurado = notBlank(falla) || notBlank(tipoServicioSugerido) || notBlank(direccion);

    if (!hayEstructurado) {
      return notBlank(descripcionLegacy) ? descripcionLegacy : (notBlank(equipo) ? "Falla pendiente de detallar" : "");
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

  // =========================
  // Compat helpers (para no romper si tu entidad aún no tiene todos los campos)
  // =========================

  private String readEquipo(TicketCrearRequest req) {
    try { return req.equipo(); } catch (Throwable e) { return null; }
  }

  private String readDescripcionFalla(TicketCrearRequest req) {
    try { return req.descripcionFalla(); } catch (Throwable e) { return null; }
  }

  private String readTipoServicioSugerido(TicketCrearRequest req) {
    try { return req.tipoServicioSugerido(); } catch (Throwable e) { return null; }
  }

  private String readDireccion(TicketCrearRequest req) {
    try { return req.direccion(); } catch (Throwable e) { return null; }
  }

  private void safeSetEquipo(TicketSolicitud t, String v) {
    try { t.getClass().getMethod("setEquipo", String.class).invoke(t, v); } catch (Throwable ignored) {}
  }

  private void safeSetDescripcionFalla(TicketSolicitud t, String v) {
    try { t.getClass().getMethod("setDescripcionFalla", String.class).invoke(t, v); } catch (Throwable ignored) {}
  }

  private void safeSetTipoServicioSugerido(TicketSolicitud t, String v) {
    try { t.getClass().getMethod("setTipoServicioSugerido", String.class).invoke(t, v); } catch (Throwable ignored) {}
  }

  private void safeSetDireccion(TicketSolicitud t, String v) {
    try { t.getClass().getMethod("setDireccion", String.class).invoke(t, v); } catch (Throwable ignored) {}
  }

  private void safeSetClienteSnapshot(TicketSolicitud t, Cliente c) {
    try { t.getClass().getMethod("setClienteNombreSnapshot", String.class).invoke(t, c.getNombre()); } catch (Throwable ignored) {}
    try { t.getClass().getMethod("setClienteTelefonoSnapshot", String.class).invoke(t, c.getTelefono()); } catch (Throwable ignored) {}
    try { t.getClass().getMethod("setClienteEmailSnapshot", String.class).invoke(t, c.getEmail()); } catch (Throwable ignored) {}
  }

  private String readClienteNombreSnapshot(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getClienteNombreSnapshot").invoke(t); } catch (Throwable e) { return null; }
  }

  private String readClienteTelefonoSnapshot(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getClienteTelefonoSnapshot").invoke(t); } catch (Throwable e) { return null; }
  }

  private String readClienteEmailSnapshot(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getClienteEmailSnapshot").invoke(t); } catch (Throwable e) { return null; }
  }

  private UUID readOrdenTrabajoId(TicketSolicitud t) {
    try { return (UUID) t.getClass().getMethod("getOrdenTrabajoId").invoke(t); } catch (Throwable e) { return null; }
  }

  private void safeSetOrdenTrabajoId(TicketSolicitud t, UUID id) {
    try { t.getClass().getMethod("setOrdenTrabajoId", UUID.class).invoke(t, id); } catch (Throwable ignored) {}
  }

  private String readEquipoField(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getEquipo").invoke(t); } catch (Throwable e) { return null; }
  }

  private String readDescripcionFallaField(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getDescripcionFalla").invoke(t); } catch (Throwable e) { return null; }
  }

  private String readTipoServicioSugeridoField(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getTipoServicioSugerido").invoke(t); } catch (Throwable e) { return null; }
  }

  private String readDireccionField(TicketSolicitud t) {
    try { return (String) t.getClass().getMethod("getDireccion").invoke(t); } catch (Throwable e) { return null; }
  }
}