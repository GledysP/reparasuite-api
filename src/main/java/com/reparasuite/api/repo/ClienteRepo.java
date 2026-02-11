package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.Cliente;

public interface ClienteRepo extends JpaRepository<Cliente, UUID> {
  Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
