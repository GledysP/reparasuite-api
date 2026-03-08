package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.PresupuestoOt;

public interface PresupuestoOtRepo extends JpaRepository<PresupuestoOt, UUID> {
  Optional<PresupuestoOt> findByOt_Id(UUID otId);
  void deleteByOt_Id(UUID otId);
}
