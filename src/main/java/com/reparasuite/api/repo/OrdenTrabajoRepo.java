package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.OrdenTrabajo;

public interface OrdenTrabajoRepo extends JpaRepository<OrdenTrabajo, UUID> {
  Page<OrdenTrabajo> findByCodigoContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(
      String codigo, String clienteNombre, Pageable pageable);
}
