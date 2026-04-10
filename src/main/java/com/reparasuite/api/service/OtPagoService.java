package com.reparasuite.api.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.PagoDto;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoPagoOt;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PagoOt;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.PresupuestoOtRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class OtPagoService {

    private final PagoOtRepo pagoRepo;
    private final OrdenTrabajoRepo otRepo;
    private final PresupuestoOtRepo presupuestoRepo;
    private final HistorialOtRepo historialRepo;
    private final SecureUploadService secureUploadService;
    private final AuthContextService authContextService;
    private final ClienteRepo clienteRepo;
    private final UsuarioRepo usuarioRepo;

    public OtPagoService(
            PagoOtRepo pagoRepo,
            OrdenTrabajoRepo otRepo,
            PresupuestoOtRepo presupuestoRepo,
            HistorialOtRepo historialRepo,
            SecureUploadService secureUploadService,
            AuthContextService authContextService,
            ClienteRepo clienteRepo,
            UsuarioRepo usuarioRepo) {
        this.pagoRepo = pagoRepo;
        this.otRepo = otRepo;
        this.presupuestoRepo = presupuestoRepo;
        this.historialRepo = historialRepo;
        this.secureUploadService = secureUploadService;
        this.authContextService = authContextService;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public void marcarTransferencia(String idOrCodigo) {
        ensureClienteForOt();
        OrdenTrabajo ot = resolverOt(idOrCodigo);
        ensureClienteOwner(ot);

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
        ensureClienteForOt();
        OrdenTrabajo ot = resolverOt(idOrCodigo);
        ensureClienteOwner(ot);

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

    // --- Métodos Privados de Utilidad ---

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

    private void ensureClienteForOt() {
        if (!authContextService.current().isCliente()) {
            throw new ForbiddenException("No autorizado");
        }
    }

    private void ensureClienteOwner(OrdenTrabajo ot) {
        if (!authContextService.current().isCliente() || !ot.getCliente().getId().equals(authContextService.current().id())) {
            throw new ForbiddenException("No autorizado");
        }
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
}