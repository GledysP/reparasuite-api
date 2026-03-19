package com.reparasuite.api.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.ClienteCrearRequest;
import com.reparasuite.api.dto.ClienteOtItemDto;
import com.reparasuite.api.dto.ClienteResumenDto;
import com.reparasuite.api.dto.ClienteUpdateRequest;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TicketFotoRepo;
import com.reparasuite.api.repo.TicketMensajeRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;

@Service
public class ClientesService {

  private static final int MAX_PAGE_SIZE = 100;

  private final ClienteRepo clienteRepo;
  private final OrdenTrabajoRepo otRepo;
  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo ticketMensajeRepo;
  private final TicketFotoRepo ticketFotoRepo;
  private final SecureUploadService secureUploadService;

  public ClientesService(
      ClienteRepo clienteRepo,
      OrdenTrabajoRepo otRepo,
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo ticketMensajeRepo,
      TicketFotoRepo ticketFotoRepo,
      SecureUploadService secureUploadService
  ) {
    this.clienteRepo = clienteRepo;
    this.otRepo = otRepo;
    this.ticketRepo = ticketRepo;
    this.ticketMensajeRepo = ticketMensajeRepo;
    this.ticketFotoRepo = ticketFotoRepo;
    this.secureUploadService = secureUploadService;
  }

  @Transactional(readOnly = true)
  public ApiListaResponse<ClienteResumenDto> listar(String query, int page, int size) {
    int pageSafe = Math.max(page, 0);
    int sizeSafe = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    Pageable pageable = PageRequest.of(
        pageSafe,
        sizeSafe,
        Sort.by(Sort.Direction.ASC, "nombre")
    );

    var p = (query == null || query.isBlank())
        ? clienteRepo.listarResumen(pageable)
        : clienteRepo.buscarResumen(query.trim(), pageable);

    return new ApiListaResponse<>(p.getContent(), p.getTotalElements());
  }

  @Transactional(readOnly = true)
  public ClienteResumenDto obtener(UUID id) {
    return clienteRepo.obtenerResumenPorId(id)
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
  }

  @Transactional(readOnly = true)
  public ApiListaResponse<ClienteOtItemDto> ordenesTrabajo(UUID clienteId, int page, int size) {
    int pageSafe = Math.max(page, 0);
    int sizeSafe = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    Pageable pageable = PageRequest.of(
        pageSafe,
        sizeSafe,
        Sort.by(Sort.Direction.DESC, "updatedAt")
    );

    var p = otRepo.findByCliente_Id(clienteId, pageable);

    return new ApiListaResponse<>(
        p.getContent().stream()
            .map(ot -> new ClienteOtItemDto(
                ot.getId(),
                ot.getCodigo(),
                ot.getEstado().name(),
                ot.getTipo().name(),
                ot.getUpdatedAt(),
                ot.getTecnico() != null ? ot.getTecnico().getNombre() : null
            ))
            .toList(),
        p.getTotalElements()
    );
  }

  @Transactional
  public ClienteResumenDto crear(ClienteCrearRequest req) {
    String nombre = cleanRequired(req.nombre(), "nombre");
    String telefono = cleanNullable(req.telefono());
    String email = normalizeEmail(req.email());

    if (email != null && clienteRepo.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("Ya existe un cliente con ese email");
    }

    Cliente c = new Cliente();
    c.setNombre(nombre);
    c.setTelefono(telefono);
    c.setEmail(email);
    c.setPortalActivo(false);

    c = clienteRepo.save(c);

    return clienteRepo.obtenerResumenPorId(c.getId())
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado tras crear"));
  }

  @Transactional
  public ClienteResumenDto actualizar(UUID id, ClienteUpdateRequest req) {
    Cliente c = clienteRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

    String nombre = cleanRequired(req.nombre(), "nombre");
    String telefono = cleanNullable(req.telefono());
    String email = normalizeEmail(req.email());

    if (email != null) {
      clienteRepo.findByEmailIgnoreCase(email).ifPresent(existing -> {
        if (!existing.getId().equals(id)) {
          throw new ConflictException("Ya existe un cliente con ese email");
        }
      });
    }

    c.setNombre(nombre);
    c.setTelefono(telefono);
    c.setEmail(email);

    clienteRepo.save(c);

    return clienteRepo.obtenerResumenPorId(c.getId())
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado tras actualizar"));
  }

  @Transactional
  public void eliminar(UUID clienteId) {
    Cliente c = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

    long totalOts = otRepo.countByCliente_Id(clienteId);
    if (totalOts > 0) {
      throw new ConflictException("No se puede eliminar el cliente porque tiene órdenes de trabajo asociadas");
    }

    var tickets = ticketRepo.findByCliente_Id(clienteId, Pageable.unpaged()).getContent();
    for (var t : tickets) {
      ticketFotoRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId())
          .forEach(f -> secureUploadService.deleteByUrl(f.getUrl()));

      ticketMensajeRepo.deleteByTicket_Id(t.getId());
      ticketFotoRepo.deleteByTicket_Id(t.getId());
      ticketRepo.delete(t);
    }

    clienteRepo.delete(c);
  }

  private String cleanNullable(String value) {
    if (value == null) return null;
    String out = value.trim();
    return out.isBlank() ? null : out;
  }

  private String cleanRequired(String value, String field) {
    String out = cleanNullable(value);
    if (out == null) {
      throw new com.reparasuite.api.exception.BadRequestException("El campo " + field + " es obligatorio");
    }
    return out;
  }

  private String normalizeEmail(String email) {
    String out = cleanNullable(email);
    return out == null ? null : out.toLowerCase(Locale.ROOT);
  }
}