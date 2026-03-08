package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.TicketMensaje;

public interface TicketMensajeRepo extends JpaRepository<TicketMensaje, UUID> {
  List<TicketMensaje> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
  void deleteByTicket_Id(UUID ticketId);
}