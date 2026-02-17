package com.reparasuite.api.service;

import java.util.UUID;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;

@Service
public class ClientesService {

  private final ClienteRepo clienteRepo;
  private final OrdenTrabajoRepo otRepo;

  public ClientesService(ClienteRepo clienteRepo, OrdenTrabajoRepo otRepo) {
    this.clienteRepo = clienteRepo;
    this.otRepo = otRepo;
  }

  public ApiListaResponse<ClienteResumenDto> listar(String query, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "nombre"));
    Page<Cliente> p;

    if (query == null || query.isBlank()) {
      p = clienteRepo.findAll(pageable);
    } else {
      p = clienteRepo.findByNombreContainingIgnoreCase(query, pageable);
    }

    return new ApiListaResponse<>(
      p.getContent().stream().map(this::toResumen).toList(),
      p.getTotalElements()
    );
  }

  public ClienteResumenDto obtener(UUID id) {
    Cliente c = clienteRepo.findById(id).orElseThrow();
    return toResumen(c);
  }

  public ApiListaResponse<ClienteOtItemDto> ordenesTrabajo(UUID clienteId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<OrdenTrabajo> p = otRepo.findByCliente_Id(clienteId, pageable);

    return new ApiListaResponse<>(
        p.getContent().stream()
            .map(ot -> new ClienteOtItemDto(ot.getCodigo(), ot.getEstado().name(), ot.getTipo().name(), ot.getUpdatedAt()))
            .toList(),
        p.getTotalElements()
    );
  }

  private ClienteResumenDto toResumen(Cliente c) {
    return new ClienteResumenDto(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail());
  }
}
