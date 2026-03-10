package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.CitaOt;

public interface CitaOtRepo extends JpaRepository<CitaOt, UUID> {

  List<CitaOt> findByOt_IdOrderByInicioAsc(UUID otId);

  void deleteByOt_Id(UUID otId);
}