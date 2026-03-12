package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.Equipo;

public interface EquipoRepo extends JpaRepository<Equipo, UUID> {

  Page<Equipo> findByEstadoActivoTrue(Pageable pageable);

  Page<Equipo> findByCliente_Id(UUID clienteId, Pageable pageable);

  Page<Equipo> findByCliente_IdAndEstadoActivo(UUID clienteId, boolean estadoActivo, Pageable pageable);

  long countByCliente_Id(UUID clienteId);

  long countByEstadoActivoTrue();

  boolean existsByCodigoEquipoIgnoreCase(String codigoEquipo);
}