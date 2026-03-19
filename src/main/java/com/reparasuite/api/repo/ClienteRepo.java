package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reparasuite.api.dto.ClienteResumenDto;
import com.reparasuite.api.model.Cliente;

public interface ClienteRepo extends JpaRepository<Cliente, UUID> {

  Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

  Optional<Cliente> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  @Query("""
      select c
      from Cliente c
      where lower(c.nombre) like lower(concat('%', :query, '%'))
         or lower(coalesce(c.telefono, '')) like lower(concat('%', :query, '%'))
         or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
      """)
  Page<Cliente> buscarPorNombreTelefonoEmail(@Param("query") String query, Pageable pageable);

  @Query(
      value = """
          select new com.reparasuite.api.dto.ClienteResumenDto(
              c.id,
              c.nombre,
              c.telefono,
              c.email,
              count(ot.id),
              max(ot.updatedAt)
          )
          from Cliente c
          left join OrdenTrabajo ot on ot.cliente = c
          group by c.id, c.nombre, c.telefono, c.email
          """,
      countQuery = """
          select count(c)
          from Cliente c
          """
  )
  Page<ClienteResumenDto> listarResumen(Pageable pageable);

  @Query(
      value = """
          select new com.reparasuite.api.dto.ClienteResumenDto(
              c.id,
              c.nombre,
              c.telefono,
              c.email,
              count(ot.id),
              max(ot.updatedAt)
          )
          from Cliente c
          left join OrdenTrabajo ot on ot.cliente = c
          where lower(c.nombre) like lower(concat('%', :query, '%'))
             or lower(coalesce(c.telefono, '')) like lower(concat('%', :query, '%'))
             or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
          group by c.id, c.nombre, c.telefono, c.email
          """,
      countQuery = """
          select count(c)
          from Cliente c
          where lower(c.nombre) like lower(concat('%', :query, '%'))
             or lower(coalesce(c.telefono, '')) like lower(concat('%', :query, '%'))
             or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
          """
  )
  Page<ClienteResumenDto> buscarResumen(@Param("query") String query, Pageable pageable);

  @Query("""
      select new com.reparasuite.api.dto.ClienteResumenDto(
          c.id,
          c.nombre,
          c.telefono,
          c.email,
          count(ot.id),
          max(ot.updatedAt)
      )
      from Cliente c
      left join OrdenTrabajo ot on ot.cliente = c
      where c.id = :id
      group by c.id, c.nombre, c.telefono, c.email
      """)
  Optional<ClienteResumenDto> obtenerResumenPorId(@Param("id") UUID id);
}