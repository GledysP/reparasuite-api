package com.reparasuite.api.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.CategoriaEquipo;

public interface CategoriaEquipoRepo extends JpaRepository<CategoriaEquipo, UUID> {
  List<CategoriaEquipo> findByActivaTrueOrderByOrdenVisualAscNombreAsc();
  Optional<CategoriaEquipo> findByCodigoIgnoreCase(String codigo);
}