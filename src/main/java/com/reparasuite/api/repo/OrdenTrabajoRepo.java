package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PrioridadOt;
import com.reparasuite.api.model.TipoOt;

public interface OrdenTrabajoRepo extends JpaRepository<OrdenTrabajo, UUID> {

  Page<OrdenTrabajo> findByCodigoContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(
      String codigo,
      String clienteNombre,
      Pageable pageable
  );

  Page<OrdenTrabajo> findByCliente_Id(UUID clienteId, Pageable pageable);

  Optional<OrdenTrabajo> findByCodigo(String codigo);

  Optional<OrdenTrabajo> findByCodigoIgnoreCase(String codigo);

  long countByCliente_Id(UUID clienteId);

  Optional<OrdenTrabajo> findTopByCliente_IdOrderByUpdatedAtDesc(UUID clienteId);

  @Query("""
      SELECT ot
      FROM OrdenTrabajo ot
      JOIN ot.cliente c
      LEFT JOIN ot.tecnico t
      LEFT JOIN ot.categoriaEquipo ce
      WHERE (
          :query IS NULL OR :query = ''
          OR LOWER(ot.codigo) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(c.telefono, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.equipo, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ot.fallaReportada, '')) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(COALESCE(ce.nombre, '')) LIKE LOWER(CONCAT('%', :query, '%'))
      )
      AND (:estado IS NULL OR ot.estado = :estado)
      AND (:tipo IS NULL OR ot.tipo = :tipo)
      AND (:prioridad IS NULL OR ot.prioridad = :prioridad)
      AND (:tecnicoId IS NULL OR t.id = :tecnicoId)
      """)
  Page<OrdenTrabajo> buscarBackoffice(
      @Param("query") String query,
      @Param("estado") EstadoOt estado,
      @Param("tipo") TipoOt tipo,
      @Param("prioridad") PrioridadOt prioridad,
      @Param("tecnicoId") UUID tecnicoId,
      Pageable pageable
  );
}