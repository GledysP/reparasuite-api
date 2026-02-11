package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.NotaOt;

public interface NotaOtRepo extends JpaRepository<NotaOt, UUID> {
  List<NotaOt> findByOt_IdOrderByCreatedAtDesc(UUID otId);
}
