package com.reparasuite.api.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.PresupuestoDto;
import com.reparasuite.api.dto.PresupuestoGuardarRequest;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.EstadoPagoOt;
import com.reparasuite.api.model.EstadoPresupuesto;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PagoOt;
import com.reparasuite.api.model.PresupuestoOt;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.PresupuestoOtRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class OtPresupuestoService {

    private final PresupuestoOtRepo presupuestoRepo;
    private final OrdenTrabajoRepo otRepo;
    private final PagoOtRepo pagoRepo;
    private final HistorialOtRepo historialRepo;
    private final OrdenTrabajoWorkflowService ordenTrabajoWorkflowService;
    private final AuthContextService authContextService;
    private final ClienteRepo clienteRepo;
    private final UsuarioRepo usuarioRepo;

    public OtPresupuestoService(
            PresupuestoOtRepo presupuestoRepo,
            OrdenTrabajoRepo otRepo,
            PagoOtRepo pagoRepo,
            HistorialOtRepo historialRepo,
            OrdenTrabajoWorkflowService ordenTrabajoWorkflowService,
            AuthContextService authContextService,
            ClienteRepo clienteRepo,
            UsuarioRepo usuarioRepo) {
        this.presupuestoRepo = presupuestoRepo;
        this.otRepo = otRepo;
        this.pagoRepo = pagoRepo;
        this.historialRepo = historialRepo;
        this.ordenTrabajoWorkflowService = ordenTrabajoWorkflowService;
        this.authContextService = authContextService;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
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
            throw new ConflictException("No se puede editar un presupuesto " + p.getEstado().name());
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
            throw new ConflictException("Presupuesto ya aceptado");
        }
        if (p.getEstado() == EstadoPresupuesto.RECHAZADO) {
            throw new ConflictException("Presupuesto rechazado: cree uno nuevo");
        }

        p.setEstado(EstadoPresupuesto.ENVIADO);
        p.setSentAt(OffsetDateTime.now());
        presupuestoRepo.save(p);

        ordenTrabajoWorkflowService.moveTo(ot, EstadoOt.PRESUPUESTO);
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
        ensureClienteForOt(idOrCodigo);
        OrdenTrabajo ot = resolverOt(idOrCodigo);
        ensureClienteOwner(ot);

        PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId())
                .orElseThrow(() -> new NotFoundException("Presupuesto no encontrado"));

        if (p.getEstado() != EstadoPresupuesto.ENVIADO) {
            throw new ConflictException("Presupuesto no aceptable en estado: " + p.getEstado().name());
        }
        if (!acepto) {
            throw new BadRequestException("Debe aceptar el check de aceptación");
        }

        p.setEstado(EstadoPresupuesto.ACEPTADO);
        p.setRespondedAt(OffsetDateTime.now());
        p.setAceptacionCheck(true);
        p.setAceptacionAt(OffsetDateTime.now());
        presupuestoRepo.save(p);

        ordenTrabajoWorkflowService.moveTo(ot, EstadoOt.APROBADA);
        otRepo.save(ot);

        registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_ACEPTADO, "Presupuesto aceptado por el cliente");
    }

    @Transactional
    public void rechazarPresupuesto(String idOrCodigo) {
        ensureClienteForOt(idOrCodigo);
        OrdenTrabajo ot = resolverOt(idOrCodigo);
        ensureClienteOwner(ot);

        PresupuestoOt p = presupuestoRepo.findByOt_Id(ot.getId())
                .orElseThrow(() -> new NotFoundException("Presupuesto no encontrado"));

        if (p.getEstado() != EstadoPresupuesto.ENVIADO) {
            throw new ConflictException("Presupuesto no rechazable en estado: " + p.getEstado().name());
        }

        p.setEstado(EstadoPresupuesto.RECHAZADO);
        p.setRespondedAt(OffsetDateTime.now());
        presupuestoRepo.save(p);

        if (ot.getEstado() != EstadoOt.PRESUPUESTO) {
            ordenTrabajoWorkflowService.moveTo(ot, EstadoOt.PRESUPUESTO);
            otRepo.save(ot);
        }

        registrarEvento(ot, EventoHistorialOt.PRESUPUESTO_RECHAZADO, "Presupuesto rechazado por el cliente");
    }

    // --- Métodos Privados de Utilidad (Copiados de tu servicio original) ---

    private PresupuestoDto toPresupuestoDto(PresupuestoOt p) {
        return new PresupuestoDto(p.getId(), p.getEstado().name(), p.getImporte(), p.getDetalle(),
                p.isAceptacionCheck(), p.getSentAt(), p.getRespondedAt());
    }

    private OrdenTrabajo resolverOt(String idOrCodigo) {
        if (idOrCodigo == null || idOrCodigo.isBlank()) {
            throw new BadRequestException("ID de orden inválido");
        }
        try {
            UUID id = UUID.fromString(idOrCodigo);
            return otRepo.findById(id).orElseThrow(() -> new NotFoundException("Orden no encontrada"));
        } catch (IllegalArgumentException ex) {
            return otRepo.findByCodigoIgnoreCase(idOrCodigo.trim())
                    .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
        }
    }

    private void registrarEvento(OrdenTrabajo ot, EventoHistorialOt evento, String descripcion) {
        HistorialOt h = new HistorialOt();
        h.setOt(ot);
        h.setEvento(evento);
        h.setDescripcion(descripcion);

        if (authContextService.current().isCliente()) {
            Cliente c = clienteRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
            h.setActorTipo(ActorTipo.CLIENTE);
            h.setActorNombre(c.getNombre());
            h.setUsuario(null);
        } else {
            Usuario u = usuarioRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            h.setActorTipo(ActorTipo.USUARIO);
            h.setActorNombre(u.getNombre());
            h.setUsuario(u);
        }
        historialRepo.save(h);
    }

    private void requireBackoffice() {
        if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }
    }

    private void ensureClienteForOt(String idOrCodigo) {
        if (!authContextService.current().isCliente()) {
            throw new ForbiddenException("No autorizado");
        }
    }

    private void ensureClienteOwner(OrdenTrabajo ot) {
        if (!authContextService.current().isCliente() || !ot.getCliente().getId().equals(authContextService.current().id())) {
            throw new ForbiddenException("No autorizado");
        }
    }
}