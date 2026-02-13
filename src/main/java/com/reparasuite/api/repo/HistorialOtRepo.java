package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.HistorialOt;

public interface HistorialOtRepo extends JpaRepository<HistorialOt, UUID> {
  List<HistorialOt> findByOt_IdOrderByFechaAsc(UUID otId);
}
