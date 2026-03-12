package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventarioItemDetalleDto(
    UUID id,
    String sku,
    String codigoBarras,
    String nombre,
    String descripcion,
    InventarioCategoriaDto categoria,
    String marca,
    String modeloCompatibilidad,
    String unidadMedida,
    String stockActual,
    String stockMinimo,
    String stockMaximo,
    boolean controlaStock,
    boolean permiteStockNegativo,
    String costoPromedio,
    String ultimoCosto,
    String precioVenta,
    String ubicacionAlmacen,
    String notas,
    boolean activo,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}