package com.reparasuite.api.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.InventarioCategoria;

public interface InventarioCategoriaRepo extends JpaRepository<InventarioCategoria, UUID> {
  List<InventarioCategoria> findByActivaTrueOrderByOrdenVisualAscNombreAsc();
  Optional<InventarioCategoria> findByCodigoIgnoreCase(String codigo);
}