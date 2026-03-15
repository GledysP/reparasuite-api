package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.PagoOt;

public interface PagoOtRepo extends JpaRepository<PagoOt, UUID> {

  Optional<PagoOt> findByOt_Id(UUID otId);

  Optional<PagoOt> findByComprobanteUrl(String comprobanteUrl);

  void deleteByOt_Id(UUID otId);
}