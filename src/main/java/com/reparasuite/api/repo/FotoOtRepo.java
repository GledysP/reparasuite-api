package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.FotoOt;

public interface FotoOtRepo extends JpaRepository<FotoOt, UUID> {
  List<FotoOt> findByOt_IdOrderByCreatedAtDesc(UUID otId);
}
