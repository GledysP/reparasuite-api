package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InventarioItemCrearRequest(
    @Size(max = 100) String sku,
    @Size(max = 100) String codigoBarras,
    @NotBlank @Size(max = 200) String nombre,
    @Size(max = 4000) String descripcion,
    String categoriaId,
    @Size(max = 120) String marca,
    @Size(max = 200) String modeloCompatibilidad,
    String unidadMedida,
    String stockMinimo,
    String stockMaximo,
    Boolean controlaStock,
    Boolean permiteStockNegativo,
    String costoPromedio,
    String ultimoCosto,
    String precioVenta,
    @Size(max = 120) String ubicacionAlmacen,
    @Size(max = 4000) String notas,
    String imagenUrl,
    Boolean activo
) {}