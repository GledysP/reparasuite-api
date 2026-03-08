package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.MensajeOt;

public interface MensajeOtRepo extends JpaRepository<MensajeOt, UUID> {
  List<MensajeOt> findByOt_IdOrderByCreatedAtAsc(UUID otId);
  void deleteByOt_Id(UUID otId);
}
