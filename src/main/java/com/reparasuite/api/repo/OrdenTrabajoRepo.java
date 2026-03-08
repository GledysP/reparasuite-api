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
      String codigo, String clienteNombre, Pageable pageable);

  Page<OrdenTrabajo> findByCliente_Id(UUID clienteId, Pageable pageable);

  Optional<OrdenTrabajo> findByCodigo(String codigo);

  // ✅ recomendado para buscar OT por código sin importar mayúsculas/minúsculas
  Optional<OrdenTrabajo> findByCodigoIgnoreCase(String codigo);

  // ✅ NUEVO: contar OTs por cliente (para ClientesService)
  long countByCliente_Id(UUID clienteId);

  // ✅ NUEVO: última OT por cliente (para ClientesService)
  Optional<OrdenTrabajo> findTopByCliente_IdOrderByUpdatedAtDesc(UUID clienteId);

  // ✅ NUEVO: listado backoffice con filtros reales
  @Query("""
	      SELECT ot
	      FROM OrdenTrabajo ot
	      JOIN ot.cliente c
	      LEFT JOIN ot.tecnico t
	      WHERE (
	          :query IS NULL OR :query = ''
	          OR CAST(ot.codigo AS string) ILIKE %:query%
	          OR CAST(c.nombre AS string) ILIKE %:query%
	          OR CAST(COALESCE(c.telefono, '') AS string) ILIKE %:query%
	          OR CAST(COALESCE(c.email, '') AS string) ILIKE %:query%
	          or CAST(COALESCE(ot.equipo, '') AS string) ILIKE %:query%
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