package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.OrdenTrabajo;

public interface OrdenTrabajoRepo extends JpaRepository<OrdenTrabajo, UUID> {
  Page<OrdenTrabajo> findByCodigoContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(
      String codigo, String clienteNombre, Pageable pageable);

  Page<OrdenTrabajo> findByCliente_Id(UUID clienteId, Pageable pageable);

  Optional<OrdenTrabajo> findByCodigo(String codigo);

  // ✅ recomendado para buscar OT por código sin importar mayúsculas/minúsculas
  Optional<OrdenTrabajo> findByCodigoIgnoreCase(String codigo);
}