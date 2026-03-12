package com.reparasuite.api.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.CategoriaEquipoFalla;

public interface CategoriaEquipoFallaRepo extends JpaRepository<CategoriaEquipoFalla, UUID> {

  List<CategoriaEquipoFalla> findByCategoriaEquipo_IdAndActivaTrueOrderByOrdenVisualAscNombreAsc(UUID categoriaId);

  Optional<CategoriaEquipoFalla> findByCategoriaEquipo_IdAndCodigoIgnoreCase(UUID categoriaId, String codigo);
}