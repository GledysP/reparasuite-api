package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.TicketSolicitud;

public interface TicketSolicitudRepo extends JpaRepository<TicketSolicitud, UUID> {
  Page<TicketSolicitud> findByCliente_Id(UUID clienteId, Pageable pageable);
}
