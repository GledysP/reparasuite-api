package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reparasuite.api.model.Cliente;

public interface ClienteRepo extends JpaRepository<Cliente, UUID> {

  Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

  Optional<Cliente> findByEmailIgnoreCase(String email);

  // ✅ NUEVO: búsqueda amplia (nombre / teléfono / email)
  @Query("""
      select c
      from Cliente c
      where lower(c.nombre) like lower(concat('%', :query, '%'))
         or lower(coalesce(c.telefono, '')) like lower(concat('%', :query, '%'))
         or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
      """)
  Page<Cliente> buscarPorNombreTelefonoEmail(@Param("query") String query, Pageable pageable);
}