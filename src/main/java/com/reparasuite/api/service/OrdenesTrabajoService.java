package com.reparasuite.api.service;

import java.io.IOException;
import java.time.OffsetDateTime;
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
import com.reparasuite.api.dto.CitaDto;
import com.reparasuite.api.dto.CitaRequest;
import com.reparasuite.api.dto.ClienteResumenDto;
import com.reparasuite.api.dto.FotoDto;
import com.reparasuite.api.dto.HistorialItemDto;
import com.reparasuite.api.dto.HistorialUsuarioDto;
import com.reparasuite.api.dto.MensajeDto;
import com.reparasuite.api.dto.NotaDto;
import com.reparasuite.api.dto.OtCrearRequest;
import com.reparasuite.api.dto.OtDetalleDto;
import com.reparasuite.api.dto.OtListaItemDto;
import com.reparasuite.api.dto.OtRevisionTecnicaRequest;
import com.reparasuite.api.dto.PagoDto;
import com.reparasuite.api.dto.PresupuestoDto;
import com.reparasuite.api.dto.PresupuestoGuardarRequest;
import com.reparasuite.api.dto.UsuarioResumenDto;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.CategoriaEquipo;
import com.reparasuite.api.model.CitaOt;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.Equipo;
import com.reparasuite.api.model.EstadoCita;
import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.EstadoPagoOt;
import com.reparasuite.api.model.EstadoPresupuesto;
import com.reparasuite.api.model.EstadoTicket;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.FotoOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.MensajeOt;
import com.reparasuite.api.model.NotaOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PagoOt;
import com.reparasuite.api.model.PresupuestoOt;
import com.reparasuite.api.model.PrioridadOt;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.model.TicketMensaje;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.model.TipoOt;
import com.reparasuite.api.model.TipoRemitente;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.CategoriaEquipoRepo;
import com.reparasuite.api.repo.CitaOtRepo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.EquipoRepo;
import com.reparasuite.api.repo.FotoOtRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.MensajeOtRepo;
import com.reparasuite.api.repo.NotaOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.PresupuestoOtRepo;
import com.reparasuite.api.repo.TallerRepo;
import com.reparasuite.api.repo.TicketMensajeRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class OrdenesTrabajoService {

  private final OrdenTrabajoRepo otRepo;
  private final ClienteRepo clienteRepo;
  private final UsuarioRepo usuarioRepo;
  private final NotaOtRepo notaRepo;
  private final FotoOtRepo fotoRepo;
  private final TallerRepo tallerRepo;
  private final HistorialOtRepo historialRepo;
  private final PresupuestoOtRepo presupuestoRepo;
  private final PagoOtRepo pagoRepo;
  private final CitaOtRepo citaRepo;
  private final MensajeOtRepo mensajeRepo;
  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo ticketMensajeRepo;
  private final EquipoRepo equipoRepo;
  private final CategoriaEquipoRepo categoriaEquipoRepo;
  private final AuthContextService authContextService;
  private final SecureUploadService secureUploadService;

  public OrdenesTrabajoService(
      OrdenTrabajoRepo otRepo,
      ClienteRepo clienteRepo,
      UsuarioRepo usuarioRepo,
      NotaOtRepo notaRepo,
      FotoOtRepo fotoRepo,
      TallerRepo tallerRepo,
      HistorialOtRepo historialRepo,
      PresupuestoOtRepo presupuestoRepo,
      PagoOtRepo pagoRepo,
      CitaOtRepo citaRepo,
      MensajeOtRepo mensajeRepo,
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo ticketMensajeRepo,
      EquipoRepo equipoRepo,
      CategoriaEquipoRepo categoriaEquipoRepo,
      AuthContextService authContextService,
      SecureUploadService secureUploadService
  ) {
    this.otRepo = otRepo;
    this.clienteRepo = clienteRepo;
    this.usuarioRepo = usuarioRepo;
    this.notaRepo = notaRepo;
    this.fotoRepo = fotoRepo;
    this.tallerRepo = tallerRepo;
    this.historialRepo = historialRepo;
    this.presupuestoRepo = presupuestoRepo;
    this.pagoRepo = pagoRepo;
    this.citaRepo = citaRepo;
    this.mensajeRepo = mensajeRepo;
    this.ticketRepo = ticketRepo;
    this.ticketMensajeRepo = ticketMensajeRepo;
    this.equipoRepo = equipoRepo;
    this.categoriaEquipoRepo = categoriaEquipoRepo;
    this.authContextService = authContextService;
    this.secureUploadService = secureUploadService;
  }

  public ApiListaResponse<OtListaItemDto> listar(String query, int page, int size) {
    return listar(query, null, null, null, null, page, size);
  }

  public ApiListaResponse<OtListaItemDto> listar(
      String query,
      String estado,
      String tipo,
      String prioridad,
      String tecnicoId,
      int page,
      int size
  ) {
    requireBackoffice();

    int pageSafe = Math.max(page, 0);
    int sizeSafe = Math.max(size, 1);

    Pageable pageable = PageRequest.of(
        pageSafe,
        sizeSafe,
        Sort.by(Sort.Direction.DESC, "updatedAt")
    );

    String q = limpiarNullable(query);
    EstadoOt estadoEnum = parseEnumNullable(EstadoOt.class, estado, "estado");
    TipoOt tipoEnum = parseEnumNullable(TipoOt.class, tipo, "tipo");
    PrioridadOt prioridadEnum = parseEnumNullable(PrioridadOt.class, prioridad, "prioridad");
    UUID tecnicoUuid = parseUuidNullable(tecnicoId, "tecnicoId");

    Page<OrdenTrabajo> p = otRepo.buscarBackoffice(q, estadoEnum, tipoEnum, prioridadEnum, tecnicoUuid, pageable);

    return new ApiListaResponse<>(
        p.getContent().stream().map(this::toListaItem).toList(),
        p.getTotalElements()
    );
  }

  public OtDetalleDto obtener(String idOrCodigo) {
    OrdenTrabajo ot = resolverOt(idOrCodigo);

    if (auth().isCliente() && !ot.getCliente().getId().equals(auth().id())) {
      throw new ForbiddenException("No autorizado");
    }

    UUID otId = ot.getId();
    boolean cliente = auth().isCliente();

    List<NotaDto> notas = notaRepo.findByOt_IdOrderByCreatedAtDesc(otId).stream()
        .filter(n -> !cliente || n.isVisibleCliente())
        .map(n -> new NotaDto(n.getId(), n.getContenido(), n.getCreatedAt()))
        .toList();

    List<FotoDto> fotos = fotoRepo.findByOt_IdOrderByCreatedAtDesc(otId).stream()
        .filter(f -> !cliente || f.isVisibleCliente())
        .map(f -> new FotoDto(f.getId(), f.getUrl(), f.getCreatedAt()))
        .toList();

    List<HistorialItemDto> historial = historialRepo.findByOt_IdOrderByFechaAsc(otId).stream()
        .map(h -> new HistorialItemDto(
            h.getFecha(),
            h.getEvento().name(),
            h.getDescripcion(),
            new HistorialUsuarioDto(h.getActorNombre())
        ))
        .toList();

    PresupuestoDto presupuesto = presupuestoRepo.findByOt_Id(otId)
        .filter(p -> !cliente || p.getEstado() != EstadoPresupuesto.BORRADOR)
        .map(p -> new PresupuestoDto(
            p.getId(),
            p.getEstado().name(),
            p.getImporte(),
            p.getDetalle(),
            p.isAceptacionCheck(),
            p.getSentAt(),
            p.getRespondedAt()
        ))
        .orElse(null);

    PagoDto pago = pagoRepo.findByOt_Id(otId)
        .map(p -> new PagoDto(
            p.getId(),
            p.getEstado().name(),
            p.getImporte(),
            p.getComprobanteUrl()
        ))
        .orElse(null);

    List<CitaDto> citas = citaRepo.findByOt_IdOrderByInicioAsc(otId).stream()
        .map(c -> new CitaDto(c.getId(), c.getInicio(), c.getFin(), c.getEstado().name()))
        .toList();

    List<MensajeDto> mensajes = mensajeRepo.findByOt_IdOrderByCreatedAtAsc(otId).stream()
        .map(m -> new MensajeDto(
            m.getId(),
            m.getRemitenteTipo().name(),
            m.getRemitenteNombre(),
            m.getContenido(),
            m.getCreatedAt()
        ))
        .toList();

    Cliente cli = ot.getCliente();
    ClienteResumenDto clienteDto = new ClienteResumenDto(
        cli.getId(),
        cli.getNombre(),
        cli.getTelefono(),
        cli.getEmail(),
        0L,
        null
    );

    Usuario tec = ot.getTecnico();
    UsuarioResumenDto tecnicoDto = null;
    if (!cliente && tec != null) {
      tecnicoDto = new UsuarioResumenDto(
          tec.getId(),
          tec.getNombre(),
          tec.getUsuario(),
          tec.getEmail(),
          tec.getRol().name(),
          tec.isActivo()
      );
    }

    return new OtDetalleDto(
        ot.getId(),
        ot.getCodigo(),
        ot.getEstado().name(),
        ot.getTipo().name(),
        ot.getPrioridad().name(),
        ot.getEquipo(),
        ot.getEquipoRegistrado() != null ? ot.getEquipoRegistrado().getId() : null,
        ot.getCategoriaEquipo() != null ? ot.getCategoriaEquipo().getId() : null,
        ot.getCategoriaEquipo() != null ? ot.getCategoriaEquipo().getNombre() : null,
        ot.getFallaReportada(),
        ot.getDescripcion(),
        ot.getFallaDetectada(),
        ot.getDiagnosticoTecnico(),
        ot.getTrabajoARealizar(),
        clienteDto,
        tecnicoDto,
        ot.getFechaPrevista(),
        ot.getDireccion(),
        ot.getNotasAcceso(),
        notas,
        fotos,
        historial,
        presupuesto,
        pago,
        citas,
        mensajes,
        ot.getCreatedAt(),
        ot.getUpdatedAt()
    );
  }

  public record CrearResponse(UUID id) {}

  @Transactional
  public CrearResponse crear(OtCrearRequest req) {
    requireBackoffice();

    Cliente cliente = resolveOrCreateCliente(req);
    Usuario tecnico = resolveTecnico(req.tecnicoId());
    Equipo equipoRegistrado = resolveEquipo(req.equipoId(), cliente.getId());
    CategoriaEquipo categoriaEquipo = resolveCategoriaEquipo(req.categoriaEquipoId());

    Taller t = tallerRepo.findById(1L)
        .orElseThrow(() -> new NotFoundException("Taller no configurado"));

    String codigo = generarCodigo(t.getPrefijoOt());

    TipoOt tipo = parseEnumRequired(TipoOt.class, req.tipo(), "tipo");
    PrioridadOt prioridad = parseEnumRequired(PrioridadOt.class, req.prioridad(), "prioridad");

    OrdenTrabajo ot = new OrdenTrabajo();
    ot.setCodigo(codigo);
    ot.setCliente(cliente);
    ot.setTecnico(tecnico);
    ot.setTipo(tipo);
    ot.setPrioridad(prioridad);
    ot.setEquipo(limpiarNullable(req.equipo()));
    ot.setEquipoRegistrado(equipoRegistrado);
    ot.setCategoriaEquipo(categoriaEquipo);
    ot.setFallaReportada(limpiarNullable(req.fallaReportada()));
    ot.setDescripcion(req.descripcion());
    ot.setEstado(EstadoOt.RECIBIDA);

    if (req.fechaPrevista() != null && !req.fechaPrevista().isBlank()) {
      ot.setFechaPrevista(OffsetDateTime.parse(req.fechaPrevista()));
    }

    ot.setDireccion(limpiarNullable(req.direccion()));
    ot.setNotasAcceso(limpiarNullable(req.notasAcceso()));

    ot = otRepo.save(ot);

    vincularTicketSiAplica(req.ticketId(), ot);

    String desc = "Orden creada (" + tipo.name() + "/" + prioridad.name() + ") para "
        + cliente.getNombre() + ": " + recortar(req.descripcion(), 200);

    registrarEvento(ot, EventoHistorialOt.OT_CREADA, desc);

    return new CrearResponse(ot.getId());
  }

  @Transactional
  public OtDetalleDto actualizarRevisionTecnica(String idOrCodigo, OtRevisionTecnicaRequest req) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);

    ot.setFallaDetectada(limpiarNullable(req.fallaDetectada()));
    ot.setDiagnosticoTecnico(limpiarNullable(req.diagnosticoTecnico()));
    ot.setTrabajoARealizar(limpiarNullable(req.trabajoARealizar()));

    otRepo.save(ot);

    registrarEvento(ot, EventoHistorialOt.NOTA_AGREGADA, "Se actualizó la revisión técnica");

    return obtener(idOrCodigo);
  }

  @Transactional
  public void cambiarEstado(String idOrCodigo, String estado) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    EstadoOt nuevo = parseEnumRequired(EstadoOt.class, estado, "estado");

    ot.setEstado(nuevo);
    otRepo.save(ot);

    registrarEvento(ot, EventoHistorialOt.CAMBIO_ESTADO, "Estado actualizado a " + nuevo.name());
  }

  @Transactional
  public void anadirNota(String idOrCodigo, String contenido, boolean visibleCliente) {
    OrdenTrabajo ot = resolverOt(idOrCodigo);

    if (auth().isCliente()) {
      requireClienteOwner(ot);
      visibleCliente = true;
    }

    NotaOt n = new NotaOt();
    n.setOt(ot);
    n.setContenido(contenido);
    n.setVisibleCliente(visibleCliente);
    notaRepo.save(n);

    registrarEvento(
        ot,
        EventoHistorialOt.NOTA_AGREGADA,
        visibleCliente ? "Se añadió una nota visible al cliente" : "Se añadió una nueva nota interna"
    );
  }

  @Transactional
  public FotoDto subirFoto(String idOrCodigo, MultipartFile file, boolean visibleCliente) throws IOException {
    OrdenTrabajo ot = resolverOt(idOrCodigo);

    if (auth().isCliente()) {
      requireClienteOwner(ot);
      visibleCliente = true;
    }

    var stored = secureUploadService.storeOtImage(ot.getId(), file);

    FotoOt f = new FotoOt();
    f.setOt(ot);
    f.setUrl(stored.url());
    f.setVisibleCliente(visibleCliente);
    f = fotoRepo.save(f);

    registrarEvento(ot, EventoHistorialOt.FOTO_SUBIDA, "Archivo subido: " + stored.originalFilename());

    return new FotoDto(f.getId(), f.getUrl(), f.getCreatedAt());
  }

  @Transactional
  public PresupuestoDto guardarPresupuesto(String idOrCodigo, PresupuestoGuardarRequest req) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);

    PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId()).orElseGet(() -> {
      PresupuestoOt x = new PresupuestoOt();
      x.setOt(ot);
      x.setEstado(EstadoPresupuesto.BORRADOR);
      return x;
    });

    if (p.getEstado() == EstadoPresupuesto.ACEPTADO || p.getEstado() == EstadoPresupuesto.RECHAZADO) {
      throw new BadRequestException("No se puede editar un presupuesto " + p.getEstado().name());
    }

    p.setImporte(req.importe());
    p.setDetalle(req.detalle());
    p = presupuestoRepo.save(p);

    registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_GUARDADO, "Presupuesto guardado (BORRADOR)");
    return toPresupuestoDto(p);
  }

  @Transactional
  public PresupuestoDto enviarPresupuesto(String idOrCodigo) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId())
        .orElseThrow(() -> new NotFoundException("Presupuesto no encontrado"));

    if (p.getEstado() == EstadoPresupuesto.ACEPTADO) {
      throw new BadRequestException("Presupuesto ya aceptado");
    }
    if (p.getEstado() == EstadoPresupuesto.RECHAZADO) {
      throw new BadRequestException("Presupuesto rechazado: cree uno nuevo (MVP)");
    }

    p.setEstado(EstadoPresupuesto.ENVIADO);
    p.setSentAt(OffsetDateTime.now());
    presupuestoRepo.save(p);

    ot.setEstado(EstadoOt.PRESUPUESTO);
    otRepo.save(ot);

    registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_ENVIADO, "Presupuesto enviado al cliente");

    final var importe = p.getImporte();
    pagoRepo.findByOt_Id(ot.getId()).orElseGet(() -> {
      PagoOt pago = new PagoOt();
      pago.setOt(ot);
      pago.setImporte(importe);
      pago.setEstado(EstadoPagoOt.PENDIENTE);
      return pagoRepo.save(pago);
    });

    return toPresupuestoDto(p);
  }

  @Transactional
  public void aceptarPresupuesto(String idOrCodigo, boolean acepto) {
    requireCliente();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    requireClienteOwner(ot);

    PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId())
        .orElseThrow(() -> new NotFoundException("Presupuesto no encontrado"));

    if (p.getEstado() != EstadoPresupuesto.ENVIADO) {
      throw new BadRequestException("Presupuesto no aceptable en estado: " + p.getEstado().name());
    }
    if (!acepto) {
      throw new BadRequestException("Debe aceptar el check de aceptación");
    }

    p.setEstado(EstadoPresupuesto.ACEPTADO);
    p.setRespondedAt(OffsetDateTime.now());
    p.setAceptacionCheck(true);
    p.setAceptacionAt(OffsetDateTime.now());
    presupuestoRepo.save(p);

    ot.setEstado(EstadoOt.APROBADA);
    otRepo.save(ot);

    registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_ACEPTADO, "Presupuesto aceptado por el cliente");
  }

  @Transactional
  public void rechazarPresupuesto(String idOrCodigo) {
    requireCliente();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    requireClienteOwner(ot);

    PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId())
        .orElseThrow(() -> new NotFoundException("Presupuesto no encontrado"));

    if (p.getEstado() != EstadoPresupuesto.ENVIADO) {
      throw new BadRequestException("Presupuesto no rechazable en estado: " + p.getEstado().name());
    }

    p.setEstado(EstadoPresupuesto.RECHAZADO);
    p.setRespondedAt(OffsetDateTime.now());
    presupuestoRepo.save(p);

    ot.setEstado(EstadoOt.PRESUPUESTO);
    otRepo.save(ot);

    registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_RECHAZADO, "Presupuesto rechazado por el cliente");
  }

  @Transactional
  public void marcarTransferencia(String idOrCodigo) {
    requireCliente();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    requireClienteOwner(ot);

    PagoOt p = pagoRepo.findByOt_Id(ot.getId()).orElseGet(() -> {
      PagoOt x = new PagoOt();
      x.setOt(ot);
      x.setImporte(java.math.BigDecimal.ZERO);
      x.setEstado(EstadoPagoOt.PENDIENTE);
      return pagoRepo.save(x);
    });

    p.setEstado(EstadoPagoOt.MARCADO_TRANSFERENCIA);
    pagoRepo.save(p);

    registrarEvento(ot, EventoHistorialOt.PAGO_MARCADO_TRANSFERENCIA, "Cliente indica pago por transferencia");
  }

  @Transactional
  public PagoDto subirComprobantePago(String idOrCodigo, MultipartFile file) throws IOException {
    requireCliente();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    requireClienteOwner(ot);

    PagoOt p = pagoRepo.findByOt_Id(ot.getId())
        .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

    var stored = secureUploadService.storePaymentReceipt(ot.getId(), file);

    p.setComprobanteUrl(stored.url());
    p.setEstado(EstadoPagoOt.COMPROBANTE_SUBIDO);
    p = pagoRepo.save(p);

    registrarEvento(ot, EventoHistorialOt.PAGO_COMPROBANTE_SUBIDO, "Comprobante de pago subido");
    return new PagoDto(p.getId(), p.getEstado().name(), p.getImporte(), p.getComprobanteUrl());
  }

  @Transactional
  public void confirmarPagoRecibido(String idOrCodigo) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);

    PagoOt p = pagoRepo.findByOt_Id(ot.getId()).orElseGet(() -> {
      PagoOt x = new PagoOt();
      x.setOt(ot);
      var presupuesto = presupuestoRepo.findByOt_Id(ot.getId()).orElse(null);
      x.setImporte(presupuesto != null ? presupuesto.getImporte() : java.math.BigDecimal.ZERO);
      x.setEstado(EstadoPagoOt.PENDIENTE);
      return pagoRepo.save(x);
    });

    EstadoPagoOt estadoConfirmado = resolveEstadoPagoConfirmado();

    if (p.getEstado() == estadoConfirmado) {
      return;
    }

    p.setEstado(estadoConfirmado);
    pagoRepo.save(p);

    registrarEvento(ot, EventoHistorialOt.PAGO_MARCADO_TRANSFERENCIA, "Backoffice confirmó pago recibido");
  }
  
  private EstadoPagoOt resolveEstadoPagoConfirmado() {
    for (String n : List.of("CONFIRMADO", "PAGADO", "RECIBIDO")) {
      try {
        return EstadoPagoOt.valueOf(n);
      } catch (IllegalArgumentException ignored) {
      }
    }
    return EstadoPagoOt.CONFIRMADO;
  }

  @Transactional
  public CitaDto reservarCita(String idOrCodigo, CitaRequest req) {
    OrdenTrabajo ot = resolverOt(idOrCodigo);

    if (auth().isCliente()) {
      requireClienteOwner(ot);
    } else if (!auth().isBackoffice()) {
      throw new ForbiddenException("No autorizado");
    }

    OffsetDateTime inicio = OffsetDateTime.parse(req.inicio());
    OffsetDateTime fin = OffsetDateTime.parse(req.fin());

    if (!fin.isAfter(inicio)) {
      throw new BadRequestException("La fecha/hora fin debe ser mayor que inicio");
    }

    CitaOt c = new CitaOt();
    c.setOt(ot);
    c.setInicio(inicio);
    c.setFin(fin);
    c.setEstado(EstadoCita.PROGRAMADA);
    c = citaRepo.save(c);

    registrarEvento(
        ot,
        EventoHistorialOt.CITA_RESERVADA,
        (auth().isCliente() ? "Cliente" : "Backoffice") + " programó cita: " + c.getInicio()
    );

    return new CitaDto(c.getId(), c.getInicio(), c.getFin(), c.getEstado().name());
  }

  @Transactional
  public CitaDto reprogramarCita(UUID citaId, CitaRequest req) {
    CitaOt c = citaRepo.findById(citaId)
        .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

    OrdenTrabajo ot = c.getOt();

    if (auth().isCliente()) {
      requireClienteOwner(ot);
    } else if (!auth().isBackoffice()) {
      throw new ForbiddenException("No autorizado");
    }

    OffsetDateTime inicio = OffsetDateTime.parse(req.inicio());
    OffsetDateTime fin = OffsetDateTime.parse(req.fin());

    if (!fin.isAfter(inicio)) {
      throw new BadRequestException("La fecha/hora fin debe ser mayor que inicio");
    }

    c.setInicio(inicio);
    c.setFin(fin);
    c.setEstado(EstadoCita.REPROGRAMADA);
    c = citaRepo.save(c);

    registrarEvento(
        ot,
        EventoHistorialOt.CITA_REPROGRAMADA,
        (auth().isCliente() ? "Cliente" : "Backoffice") + " reprogramó cita: " + c.getInicio()
    );

    return new CitaDto(c.getId(), c.getInicio(), c.getFin(), c.getEstado().name());
  }

  @Transactional
  public MensajeDto enviarMensaje(String idOrCodigo, String contenido) {
    OrdenTrabajo ot = resolverOt(idOrCodigo);

    if (auth().isCliente()) {
      requireClienteOwner(ot);
    }

    MensajeOt m = new MensajeOt();
    m.setOt(ot);

    if (auth().isCliente()) {
      Cliente c = clienteRepo.findById(auth().id())
          .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
      m.setRemitenteTipo(TipoRemitente.CLIENTE);
      m.setRemitenteNombre(c.getNombre());
    } else {
      Usuario u = usuarioRepo.findById(auth().id())
          .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
      m.setRemitenteTipo(TipoRemitente.USUARIO);
      m.setRemitenteNombre(u.getNombre());
    }

    m.setContenido(contenido);
    m = mensajeRepo.save(m);

    registrarEvento(ot, EventoHistorialOt.MENSAJE_ENVIADO, "Mensaje enviado");
    return new MensajeDto(
        m.getId(),
        m.getRemitenteTipo().name(),
        m.getRemitenteNombre(),
        m.getContenido(),
        m.getCreatedAt()
    );
  }

  public record CrearDesdeTicketResponse(UUID id, String codigo) {}

  @Transactional
  public CrearDesdeTicketResponse crearDesdeTicket(TicketSolicitud ticket) {
    Taller t = tallerRepo.findById(1L)
        .orElseThrow(() -> new NotFoundException("Taller no configurado"));

    String codigo = generarCodigo(t.getPrefijoOt());

    TipoOt tipo = parseTipoTicket(ticket.getTipoServicioSugerido());
    PrioridadOt prioridad = PrioridadOt.MEDIA;

    String equipo = limpiarNullable(ticket.getEquipo());
    if (equipo == null) {
      equipo = limpiarNullable(ticket.getAsunto());
    }

    String falla = limpiarNullable(ticket.getDescripcionFalla());
    String descripcionLimpia = (falla != null && !falla.isBlank())
        ? falla
        : limpiarDescripcionDesdeTicket(ticket.getDescripcion());

    OrdenTrabajo ot = new OrdenTrabajo();
    ot.setCodigo(codigo);
    ot.setCliente(ticket.getCliente());
    ot.setTecnico(null);
    ot.setTipo(tipo);
    ot.setPrioridad(prioridad);
    ot.setEquipo(equipo);
    ot.setFallaReportada(falla);
    ot.setDescripcion(descripcionLimpia);
    ot.setEstado(EstadoOt.RECIBIDA);

    String direccion = limpiarNullable(ticket.getDireccion());
    if (tipo == TipoOt.DOMICILIO && direccion != null) {
      ot.setDireccion(direccion);
    }

    ot = otRepo.save(ot);

    registrarEvento(
        ot,
        EventoHistorialOt.OT_CREADA,
        "OT creada desde ticket: " + (equipo != null ? equipo : ticket.getAsunto())
    );

    return new CrearDesdeTicketResponse(ot.getId(), ot.getCodigo());
  }

  @Transactional
  public void eliminar(String idOrCodigo) {
    requireBackoffice();

    OrdenTrabajo ot = resolverOt(idOrCodigo);
    UUID otId = ot.getId();

    List<TicketSolicitud> tickets = ticketRepo.findByOrdenTrabajoId(otId);

    for (TicketSolicitud t : tickets) {
      t.setOrdenTrabajoId(null);
      if (t.getEstado() == EstadoTicket.EN_REVISION) {
        t.setEstado(EstadoTicket.ABIERTO);
      }
      ticketRepo.save(t);
    }

    fotoRepo.findByOt_IdOrderByCreatedAtDesc(otId)
        .forEach(f -> secureUploadService.deleteByUrl(f.getUrl()));

    pagoRepo.findByOt_Id(otId)
        .ifPresent(p -> secureUploadService.deleteByUrl(p.getComprobanteUrl()));

    mensajeRepo.deleteByOt_Id(otId);
    citaRepo.deleteByOt_Id(otId);
    historialRepo.deleteByOt_Id(otId);
    fotoRepo.deleteByOt_Id(otId);
    notaRepo.deleteByOt_Id(otId);
    pagoRepo.deleteByOt_Id(otId);
    presupuestoRepo.deleteByOt_Id(otId);

    otRepo.delete(ot);
  }

  private PresupuestoDto toPresupuestoDto(PresupuestoOt p) {
    return new PresupuestoDto(
        p.getId(),
        p.getEstado().name(),
        p.getImporte(),
        p.getDetalle(),
        p.isAceptacionCheck(),
        p.getSentAt(),
        p.getRespondedAt()
    );
  }

  private void registrarEvento(OrdenTrabajo ot, EventoHistorialOt evento, String descripcion) {
    HistorialOt h = new HistorialOt();
    h.setOt(ot);
    h.setEvento(evento);
    h.setDescripcion(descripcion);

    if (auth().isCliente()) {
      Cliente c = clienteRepo.findById(auth().id())
          .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
      h.setActorTipo(ActorTipo.CLIENTE);
      h.setActorNombre(c.getNombre());
      h.setUsuario(null);
    } else {
      Usuario u = usuarioRepo.findById(auth().id())
          .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
      h.setActorTipo(ActorTipo.USUARIO);
      h.setActorNombre(u.getNombre());
      h.setUsuario(u);
    }

    historialRepo.save(h);
  }

  private OrdenTrabajo resolverOt(String idOrCodigo) {
    if (idOrCodigo == null || idOrCodigo.isBlank()) {
      throw new BadRequestException("ID de orden inválido");
    }

    try {
      UUID id = UUID.fromString(idOrCodigo);
      return otRepo.findById(id)
          .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
    } catch (IllegalArgumentException ex) {
      return otRepo.findByCodigoIgnoreCase(idOrCodigo.trim())
          .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
    }
  }

  private Cliente resolveOrCreateCliente(OtCrearRequest req) {
    if (req.cliente().id() != null && !req.cliente().id().isBlank()) {
      Cliente cliente = clienteRepo.findById(UUID.fromString(req.cliente().id()))
          .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

      if (req.cliente().nombre() != null && !req.cliente().nombre().isBlank()) {
        cliente.setNombre(req.cliente().nombre().trim());
      }
      if (req.cliente().telefono() != null) {
        cliente.setTelefono(limpiarNullable(req.cliente().telefono()));
      }
      cliente.setEmail(normalizarEmail(req.cliente().email()));
      return clienteRepo.save(cliente);
    }

    String emailNorm = normalizarEmail(req.cliente().email());
    Cliente cliente = emailNorm != null
        ? clienteRepo.findByEmailIgnoreCase(emailNorm).orElse(null)
        : null;

    if (cliente == null) {
      cliente = new Cliente();
    }

    cliente.setNombre(req.cliente().nombre());
    cliente.setTelefono(req.cliente().telefono());
    cliente.setEmail(emailNorm);
    return clienteRepo.save(cliente);
  }

  private Usuario resolveTecnico(String tecnicoId) {
    if (tecnicoId == null || tecnicoId.isBlank()) return null;

    return usuarioRepo.findById(UUID.fromString(tecnicoId))
        .orElseThrow(() -> new NotFoundException("Técnico no encontrado"));
  }

  private Equipo resolveEquipo(String equipoId, UUID clienteId) {
    if (equipoId == null || equipoId.isBlank()) return null;

    Equipo equipo = equipoRepo.findById(UUID.fromString(equipoId))
        .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));

    if (!equipo.getCliente().getId().equals(clienteId)) {
      throw new BadRequestException("El equipo seleccionado no pertenece al cliente");
    }

    return equipo;
  }

  private CategoriaEquipo resolveCategoriaEquipo(String categoriaEquipoId) {
    if (categoriaEquipoId == null || categoriaEquipoId.isBlank()) return null;

    return categoriaEquipoRepo.findById(UUID.fromString(categoriaEquipoId))
        .orElseThrow(() -> new NotFoundException("Categoría de equipo no encontrada"));
  }

  private void vincularTicketSiAplica(String ticketId, OrdenTrabajo ot) {
    if (ticketId == null || ticketId.isBlank()) return;

    TicketSolicitud ticket = ticketRepo.findById(UUID.fromString(ticketId))
        .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

    ticket.setOrdenTrabajoId(ot.getId());

    if (ticket.getEstado() == EstadoTicket.ABIERTO) {
      ticket.setEstado(EstadoTicket.EN_REVISION);
    }

    ticketRepo.save(ticket);

    TicketMensaje m = new TicketMensaje();
    m.setTicket(ticket);
    m.setRemitenteTipo(TipoRemitente.USUARIO);
    m.setRemitenteNombre(auth().displayName());
    m.setContenido("Se creó una OT desde este ticket: " + ot.getCodigo());
    ticketMensajeRepo.save(m);
  }

  private void requireBackoffice() {
    if (!auth().isBackoffice()) {
      throw new ForbiddenException("No autorizado");
    }
  }

  private void requireCliente() {
    if (!auth().isCliente()) {
      throw new ForbiddenException("No autorizado");
    }
  }

  private void requireClienteOwner(OrdenTrabajo ot) {
    if (!ot.getCliente().getId().equals(auth().id())) {
      throw new ForbiddenException("No autorizado");
    }
  }

  private TipoOt parseTipoTicket(String raw) {
    if (raw == null || raw.isBlank()) return TipoOt.DOMICILIO;
    try {
      return TipoOt.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return TipoOt.DOMICILIO;
    }
  }

  private String generarCodigo(String prefijo) {
    long num = System.currentTimeMillis() % 100000;
    return prefijo + "-" + String.format("%05d", num);
  }

  private String recortar(String s, int max) {
    if (s == null) return "";
    String t = s.trim();
    if (t.length() <= max) return t;
    return t.substring(0, max - 1) + "…";
  }

  private OtListaItemDto toListaItem(OrdenTrabajo ot) {
    return new OtListaItemDto(
        ot.getId(),
        ot.getCodigo(),
        ot.getEstado().name(),
        ot.getTipo().name(),
        ot.getPrioridad().name(),
        ot.getEquipo(),
        ot.getEquipoRegistrado() != null ? ot.getEquipoRegistrado().getId() : null,
        ot.getCategoriaEquipo() != null ? ot.getCategoriaEquipo().getId() : null,
        ot.getCategoriaEquipo() != null ? ot.getCategoriaEquipo().getNombre() : null,
        ot.getFallaReportada(),
        ot.getCliente().getNombre(),
        ot.getTecnico() != null ? ot.getTecnico().getNombre() : null,
        ot.getUpdatedAt()
    );
  }

  private String normalizarEmail(String email) {
    if (email == null) return null;
    String x = email.trim().toLowerCase();
    return x.isBlank() ? null : x;
  }

  private String limpiarNullable(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isBlank() ? null : t;
  }

  private String limpiarDescripcionDesdeTicket(String descripcionTicket) {
    if (descripcionTicket == null || descripcionTicket.isBlank()) {
      return "Solicitud creada desde ticket";
    }

    String[] lines = descripcionTicket.split("\\r?\\n");
    StringBuilder sb = new StringBuilder();

    for (String line : lines) {
      String l = line == null ? "" : line.trim();
      if (l.isBlank()) continue;

      String low = l.toLowerCase();
      if (low.startsWith("equipo:")) continue;
      if (low.startsWith("equipo / asunto:")) continue;
      if (low.startsWith("tipo sugerido:")) continue;
      if (low.startsWith("dirección / ubicación:")) continue;
      if (low.startsWith("direccion / ubicacion:")) continue;
      if (low.startsWith("nota:")) continue;
      if (low.startsWith("observaciones:")) continue;

      if (sb.length() > 0) sb.append("\n");
      sb.append(l);
    }

    String out = sb.toString().trim();
    return out.isBlank() ? "Solicitud creada desde ticket" : out;
  }

  private <E extends Enum<E>> E parseEnumNullable(Class<E> enumType, String raw, String fieldName) {
    if (raw == null || raw.isBlank()) return null;
    try {
      return Enum.valueOf(enumType, raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Valor inválido para " + fieldName + ": " + raw);
    }
  }

  private <E extends Enum<E>> E parseEnumRequired(Class<E> enumType, String raw, String fieldName) {
    E value = parseEnumNullable(enumType, raw, fieldName);
    if (value == null) {
      throw new BadRequestException("Falta valor para " + fieldName);
    }
    return value;
  }

  private UUID parseUuidNullable(String raw, String fieldName) {
    if (raw == null || raw.isBlank()) return null;
    try {
      return UUID.fromString(raw.trim());
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("UUID inválido para " + fieldName + ": " + raw);
    }
  }

  private AuthContextService.AuthUser auth() {
    return authContextService.current();
  }
}