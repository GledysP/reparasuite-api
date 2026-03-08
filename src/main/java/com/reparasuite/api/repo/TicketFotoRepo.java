package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.TicketFoto;

public interface TicketFotoRepo extends JpaRepository<TicketFoto, UUID> {
  List<TicketFoto> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
  void deleteByTicket_Id(UUID ticketId);
}