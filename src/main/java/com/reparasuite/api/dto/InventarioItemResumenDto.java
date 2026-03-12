package com.reparasuite.api.dto;

import java.util.UUID;

public record InventarioItemResumenDto(
    UUID id,
    String sku,
    String nombre,
    String categoriaNombre,
    String marca,
    String unidadMedida,
    String stockActual,
    String stockMinimo,
    String costoPromedio,
    String precioVenta,
    boolean activo
) {}