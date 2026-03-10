package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.EstadoTicket;
import com.reparasuite.api.model.TicketSolicitud;

public interface TicketSolicitudRepo extends JpaRepository<TicketSolicitud, UUID> {

  Page<TicketSolicitud> findByCliente_Id(UUID clienteId, Pageable pageable);

  Page<TicketSolicitud> findByEstado(EstadoTicket estado, Pageable pageable);

  Page<TicketSolicitud> findByAsuntoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
      String asunto,
      String descripcion,
      Pageable pageable
  );

  List<TicketSolicitud> findByOrdenTrabajoId(UUID ordenTrabajoId);

  long countByCliente_Id(UUID clienteId);
}