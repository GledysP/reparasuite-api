package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reparasuite.api.model.Equipo;

public interface EquipoRepo extends JpaRepository<Equipo, UUID> {

  Page<Equipo> findByEstadoActivoTrue(Pageable pageable);

  Page<Equipo> findByCliente_Id(UUID clienteId, Pageable pageable);

  Page<Equipo> findByCliente_IdAndEstadoActivo(UUID clienteId, boolean estadoActivo, Pageable pageable);

  long countByCliente_Id(UUID clienteId);

  long countByEstadoActivoTrue();

  boolean existsByCodigoEquipoIgnoreCase(String codigoEquipo);

  @Query("""
      SELECT DISTINCT e
      FROM Equipo e
      JOIN e.cliente c
      LEFT JOIN e.categoriaEquipo ce
      LEFT JOIN OrdenTrabajo ot ON ot.equipoRegistrado = e
      WHERE (:clienteId IS NULL OR c.id = :clienteId)
        AND (:activo IS NULL OR e.estadoActivo = :activo)
        AND (
          :query IS NULL OR :query = ''
          OR LOWER(COALESCE(e.codigoEquipo, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.codigoInterno, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.tipoEquipo, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.marca, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.modelo, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.numeroSerie, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.descripcionGeneral, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(e.notasTecnicas, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ce.nombre, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.codigo, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.fallaReportada, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.fallaDetectada, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.diagnosticoTecnico, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.trabajoARealizar, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      """)
  Page<Equipo> buscarConHistorial(
      @Param("query") String query,
      @Param("clienteId") UUID clienteId,
      @Param("activo") Boolean activo,
      Pageable pageable
  );
}