package com.reparasuite.api.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.CitaDto;
import com.reparasuite.api.dto.CitaRequest;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.CitaOt;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoCita;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.CitaOtRepo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class OtCitaService {

    private final CitaOtRepo citaRepo;
    private final OrdenTrabajoRepo otRepo;
    private final HistorialOtRepo historialRepo;
    private final AuthContextService authContextService;
    private final ClienteRepo clienteRepo;
    private final UsuarioRepo usuarioRepo;

    public OtCitaService(
            CitaOtRepo citaRepo,
            OrdenTrabajoRepo otRepo,
            HistorialOtRepo historialRepo,
            AuthContextService authContextService,
            ClienteRepo clienteRepo,
            UsuarioRepo usuarioRepo) {
        this.citaRepo = citaRepo;
        this.otRepo = otRepo;
        this.historialRepo = historialRepo;
        this.authContextService = authContextService;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public CitaDto reservarCita(String idOrCodigo, CitaRequest req) {
        OrdenTrabajo ot = resolverOt(idOrCodigo);

        if (authContextService.current().isCliente()) {
            ensureClienteOwner(ot);
        } else if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }

        OffsetDateTime inicio = parseOffsetDateTime(req.inicio(), "inicio");
        OffsetDateTime fin = parseOffsetDateTime(req.fin(), "fin");

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
                (authContextService.current().isCliente() ? "Cliente" : "Backoffice") + " programó cita: " + c.getInicio()
        );

        return new CitaDto(c.getId(), c.getInicio(), c.getFin(), c.getEstado().name());
    }

    @Transactional
    public CitaDto reprogramarCita(UUID citaId, CitaRequest req) {
        CitaOt c = citaRepo.findById(citaId)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        OrdenTrabajo ot = c.getOt();

        if (authContextService.current().isCliente()) {
            ensureClienteOwner(ot);
        } else if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }

        OffsetDateTime inicio = parseOffsetDateTime(req.inicio(), "inicio");
        OffsetDateTime fin = parseOffsetDateTime(req.fin(), "fin");

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
                (authContextService.current().isCliente() ? "Cliente" : "Backoffice") + " reprogramó cita: " + c.getInicio()
        );

        return new CitaDto(c.getId(), c.getInicio(), c.getFin(), c.getEstado().name());
    }

    // --- Utilidades ---

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

    private void ensureClienteOwner(OrdenTrabajo ot) {
        if (!authContextService.current().isCliente() || !ot.getCliente().getId().equals(authContextService.current().id())) {
            throw new ForbiddenException("No autorizado");
        }
    }

    private OffsetDateTime parseOffsetDateTime(String raw, String fieldName) {
        try {
            return OffsetDateTime.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Fecha inválida para " + fieldName + ": " + raw);
        }
    }
}