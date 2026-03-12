package com.reparasuite.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.ClienteOtItemDto;
import com.reparasuite.api.dto.ClienteResumenDto;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TicketFotoRepo;
import com.reparasuite.api.repo.TicketMensajeRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;

@Service
public class ClientesService {

  private final ClienteRepo clienteRepo;
  private final OrdenTrabajoRepo otRepo;
  private final TicketSolicitudRepo ticketRepo;
  private final TicketMensajeRepo ticketMensajeRepo;
  private final TicketFotoRepo ticketFotoRepo;

  public ClientesService(
      ClienteRepo clienteRepo,
      OrdenTrabajoRepo otRepo,
      TicketSolicitudRepo ticketRepo,
      TicketMensajeRepo ticketMensajeRepo,
      TicketFotoRepo ticketFotoRepo
  ) {
    this.clienteRepo = clienteRepo;
    this.otRepo = otRepo;
    this.ticketRepo = ticketRepo;
    this.ticketMensajeRepo = ticketMensajeRepo;
    this.ticketFotoRepo = ticketFotoRepo;
  }

  public ApiListaResponse<ClienteResumenDto> listar(String query, int page, int size) {
    int pageSafe = Math.max(page, 0);
    int sizeSafe = Math.max(size, 1);

    Pageable pageable = PageRequest.of(
        pageSafe,
        sizeSafe,
        Sort.by(Sort.Direction.ASC, "nombre")
    );

    Page<Cliente> p;
    if (query == null || query.isBlank()) {
      p = clienteRepo.findAll(pageable);
    } else {
      p = clienteRepo.buscarPorNombreTelefonoEmail(query.trim(), pageable);
    }

    return new ApiListaResponse<>(
        p.getContent().stream().map(this::toResumen).toList(),
        p.getTotalElements()
    );
  }

  public ClienteResumenDto obtener(UUID id) {
    Cliente c = clienteRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    return toResumen(c);
  }

  public ApiListaResponse<ClienteOtItemDto> ordenesTrabajo(UUID clienteId, int page, int size) {
    int pageSafe = Math.max(page, 0);
    int sizeSafe = Math.max(size, 1);

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
  public void eliminar(UUID clienteId) {
    Cliente c = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    long totalOts = otRepo.countByCliente_Id(clienteId);
    if (totalOts > 0) {
      throw new IllegalStateException("No se puede eliminar el cliente porque tiene órdenes de trabajo asociadas");
    }

    var tickets = ticketRepo.findByCliente_Id(clienteId, Pageable.unpaged()).getContent();
    for (var t : tickets) {
      ticketMensajeRepo.deleteByTicket_Id(t.getId());
      ticketFotoRepo.deleteByTicket_Id(t.getId());
      ticketRepo.delete(t);
    }

    clienteRepo.delete(c);
  }

  private ClienteResumenDto toResumen(Cliente c) {
    long totalWos = otRepo.countByCliente_Id(c.getId());
    var lastOt = otRepo.findTopByCliente_IdOrderByUpdatedAtDesc(c.getId()).orElse(null);

    return new ClienteResumenDto(
        c.getId(),
        c.getNombre(),
        c.getTelefono(),
        c.getEmail(),
        totalWos,
        lastOt != null ? lastOt.getUpdatedAt() : null
    );
  }
}