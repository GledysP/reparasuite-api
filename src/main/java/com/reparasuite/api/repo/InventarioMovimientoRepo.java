package com.reparasuite.api.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.InventarioMovimiento;

public interface InventarioMovimientoRepo extends JpaRepository<InventarioMovimiento, UUID> {
  List<InventarioMovimiento> findByInventarioItem_IdOrderByFechaMovimientoDesc(UUID itemId);
}