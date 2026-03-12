package com.reparasuite.api.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.InventarioItem;

public interface InventarioItemRepo extends JpaRepository<InventarioItem, UUID> {
  Page<InventarioItem> findByActivoTrue(Pageable pageable);
  boolean existsBySkuIgnoreCase(String sku);
}