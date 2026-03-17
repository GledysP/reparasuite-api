package com.reparasuite.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.InventarioCategoriaDto;
import com.reparasuite.api.dto.InventarioItemCrearRequest;
import com.reparasuite.api.dto.InventarioItemDetalleDto;
import com.reparasuite.api.dto.InventarioItemResumenDto;
import com.reparasuite.api.dto.InventarioMovimientoCrearRequest;
import com.reparasuite.api.dto.InventarioMovimientoDto;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ConflictException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.InventarioCategoria;
import com.reparasuite.api.model.InventarioItem;
import com.reparasuite.api.model.InventarioMovimiento;
import com.reparasuite.api.model.TipoMovimientoInventario;
import com.reparasuite.api.model.UnidadMedidaInventario;
import com.reparasuite.api.repo.InventarioCategoriaRepo;
import com.reparasuite.api.repo.InventarioItemRepo;
import com.reparasuite.api.repo.InventarioMovimientoRepo;

@Service
public class InventarioService {

  private final InventarioItemRepo itemRepo;
  private final InventarioCategoriaRepo categoriaRepo;
  private final InventarioMovimientoRepo movimientoRepo;

  public InventarioService(
      InventarioItemRepo itemRepo,
      InventarioCategoriaRepo categoriaRepo,
      InventarioMovimientoRepo movimientoRepo
  ) {
    this.itemRepo = itemRepo;
    this.categoriaRepo = categoriaRepo;
    this.movimientoRepo = movimientoRepo;
  }

  public ApiListaResponse<InventarioItemResumenDto> listar(Boolean activo, int page, int size) {
    Pageable pageable = PageRequest.of(
        Math.max(page, 0),
        Math.max(size, 1),
        Sort.by(Sort.Direction.ASC, "nombre")
    );

    Page<InventarioItem> p = activo == null
        ? itemRepo.findAll(pageable)
        : (activo ? itemRepo.findByActivoTrue(pageable) : itemRepo.findAll(pageable));

    return new ApiListaResponse<>(
        p.getContent().stream().map(this::toResumen).toList(),
        p.getTotalElements()
    );
  }

  public InventarioItemDetalleDto obtener(UUID id) {
    InventarioItem i = itemRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Item no encontrado"));
    return toDetalle(i);
  }

  @Transactional
  public InventarioItemDetalleDto crear(InventarioItemCrearRequest req) {
    String sku = normalizeRequired(req.sku(), "sku");

    if (itemRepo.existsBySkuIgnoreCase(sku)) {
      throw new ConflictException("Ya existe un item con ese SKU");
    }

    InventarioItem i = new InventarioItem();
    aplicarReq(i, req);
    i.setSku(sku);
    i.setStockActual(BigDecimal.ZERO);
    i = itemRepo.save(i);
    return toDetalle(i);
  }

  @Transactional
  public InventarioItemDetalleDto actualizar(UUID id, InventarioItemCrearRequest req) {
    InventarioItem i = itemRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Item no encontrado"));

    String skuNuevo = normalizeRequired(req.sku(), "sku");
    if (!i.getSku().equalsIgnoreCase(skuNuevo) && itemRepo.existsBySkuIgnoreCase(skuNuevo)) {
      throw new ConflictException("Ya existe un item con ese SKU");
    }

    BigDecimal stockActual = i.getStockActual();
    aplicarReq(i, req);
    i.setSku(skuNuevo);
    i.setStockActual(stockActual);
    i = itemRepo.save(i);
    return toDetalle(i);
  }

  public List<InventarioCategoriaDto> categorias() {
    return categoriaRepo.findByActivaTrueOrderByOrdenVisualAscNombreAsc().stream()
        .map(c -> new InventarioCategoriaDto(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion()))
        .toList();
  }

  public List<InventarioMovimientoDto> movimientos(UUID itemId) {
    return movimientoRepo.findByInventarioItem_IdOrderByFechaMovimientoDesc(itemId).stream()
        .map(this::toMovimientoDto)
        .toList();
  }

  @Transactional
  public InventarioMovimientoDto registrarMovimiento(UUID itemId, InventarioMovimientoCrearRequest req) {
    InventarioItem item = itemRepo.findById(itemId)
        .orElseThrow(() -> new NotFoundException("Item no encontrado"));

    TipoMovimientoInventario tipo = parseTipoMovimiento(req.tipoMovimiento());
    BigDecimal cantidad = parseDecimal(req.cantidad(), "cantidad");
    if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("La cantidad debe ser mayor que cero");
    }

    BigDecimal stockAnterior = item.getStockActual() == null ? BigDecimal.ZERO : item.getStockActual();
    BigDecimal stockResultante = calcularStockResultante(stockAnterior, cantidad, tipo);

    if (item.isControlaStock() && !item.isPermiteStockNegativo() && stockResultante.compareTo(BigDecimal.ZERO) < 0) {
      throw new ConflictException("El movimiento dejaría stock negativo");
    }

    item.setStockActual(stockResultante);

    if (tipo == TipoMovimientoInventario.ENTRADA && req.costoUnitario() != null && !req.costoUnitario().isBlank()) {
      BigDecimal costo = parseDecimal(req.costoUnitario(), "costoUnitario");
      item.setUltimoCosto(costo);
      if (item.getCostoPromedio() == null || item.getCostoPromedio().compareTo(BigDecimal.ZERO) == 0) {
        item.setCostoPromedio(costo);
      }
    }

    item = itemRepo.save(item);

    InventarioMovimiento mov = new InventarioMovimiento();
    mov.setInventarioItem(item);
    mov.setTipoMovimiento(tipo);
    mov.setCantidad(cantidad);
    mov.setStockAnterior(stockAnterior);
    mov.setStockResultante(stockResultante);
    mov.setCostoUnitario(req.costoUnitario() != null && !req.costoUnitario().isBlank()
        ? parseDecimal(req.costoUnitario(), "costoUnitario")
        : null);
    mov.setMotivo(limpiarNullable(req.motivo()));
    mov.setObservacion(limpiarNullable(req.observacion()));
    mov.setReferenciaTipo("MANUAL");

    mov = movimientoRepo.save(mov);
    return toMovimientoDto(mov);
  }

  private void aplicarReq(InventarioItem i, InventarioItemCrearRequest req) {
    i.setCodigoBarras(limpiarNullable(req.codigoBarras()));
    i.setNombre(normalizeRequired(req.nombre(), "nombre"));
    i.setDescripcion(limpiarNullable(req.descripcion()));
    i.setCategoria(resolveCategoria(req.categoriaId()));
    i.setMarca(limpiarNullable(req.marca()));
    i.setModeloCompatibilidad(limpiarNullable(req.modeloCompatibilidad()));
    i.setUnidadMedida(resolveUnidad(req.unidadMedida()));
    i.setStockMinimo(parseDecimalNullable(req.stockMinimo(), BigDecimal.ZERO));
    i.setStockMaximo(parseDecimalNullable(req.stockMaximo(), null));
    i.setControlaStock(req.controlaStock() == null ? true : req.controlaStock());
    i.setPermiteStockNegativo(req.permiteStockNegativo() == null ? false : req.permiteStockNegativo());
    i.setCostoPromedio(parseDecimalNullable(req.costoPromedio(), BigDecimal.ZERO));
    i.setUltimoCosto(parseDecimalNullable(req.ultimoCosto(), BigDecimal.ZERO));
    i.setPrecioVenta(parseDecimalNullable(req.precioVenta(), BigDecimal.ZERO));
    i.setUbicacionAlmacen(limpiarNullable(req.ubicacionAlmacen()));
    i.setNotas(limpiarNullable(req.notas()));
    i.setActivo(req.activo() == null ? true : req.activo());
  }

  private InventarioCategoria resolveCategoria(String categoriaId) {
    if (categoriaId == null || categoriaId.isBlank()) return null;
    return categoriaRepo.findById(UUID.fromString(categoriaId.trim()))
        .orElseThrow(() -> new NotFoundException("Categoría de inventario no encontrada"));
  }

  private UnidadMedidaInventario resolveUnidad(String raw) {
    if (raw == null || raw.isBlank()) return UnidadMedidaInventario.UNIDAD;
    try {
      return UnidadMedidaInventario.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Unidad de medida inválida: " + raw);
    }
  }

  private TipoMovimientoInventario parseTipoMovimiento(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("El tipoMovimiento es obligatorio");
    }

    try {
      return TipoMovimientoInventario.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Tipo de movimiento inválido: " + raw);
    }
  }

  private BigDecimal calcularStockResultante(BigDecimal stockAnterior, BigDecimal cantidad, TipoMovimientoInventario tipo) {
    return switch (tipo) {
      case ENTRADA, AJUSTE_POSITIVO, DEVOLUCION_OT -> stockAnterior.add(cantidad);
      case SALIDA, AJUSTE_NEGATIVO, CONSUMO_OT, VENTA_DIRECTA -> stockAnterior.subtract(cantidad);
    };
  }

  private InventarioItemResumenDto toResumen(InventarioItem i) {
    return new InventarioItemResumenDto(
        i.getId(),
        i.getSku(),
        i.getNombre(),
        i.getCategoria() != null ? i.getCategoria().getNombre() : null,
        i.getMarca(),
        i.getUnidadMedida().name(),
        decimalStr(i.getStockActual()),
        decimalStr(i.getStockMinimo()),
        decimalStr(i.getCostoPromedio()),
        decimalStr(i.getPrecioVenta()),
        i.isActivo()
    );
  }

  private InventarioItemDetalleDto toDetalle(InventarioItem i) {
    return new InventarioItemDetalleDto(
        i.getId(),
        i.getSku(),
        i.getCodigoBarras(),
        i.getNombre(),
        i.getDescripcion(),
        i.getCategoria() != null
            ? new InventarioCategoriaDto(
                i.getCategoria().getId(),
                i.getCategoria().getCodigo(),
                i.getCategoria().getNombre(),
                i.getCategoria().getDescripcion()
            )
            : null,
        i.getMarca(),
        i.getModeloCompatibilidad(),
        i.getUnidadMedida().name(),
        decimalStr(i.getStockActual()),
        decimalStr(i.getStockMinimo()),
        decimalStr(i.getStockMaximo()),
        i.isControlaStock(),
        i.isPermiteStockNegativo(),
        decimalStr(i.getCostoPromedio()),
        decimalStr(i.getUltimoCosto()),
        decimalStr(i.getPrecioVenta()),
        i.getUbicacionAlmacen(),
        i.getNotas(),
        i.isActivo(),
        i.getCreatedAt(),
        i.getUpdatedAt()
    );
  }

  private InventarioMovimientoDto toMovimientoDto(InventarioMovimiento m) {
    return new InventarioMovimientoDto(
        m.getId(),
        m.getTipoMovimiento().name(),
        decimalStr(m.getCantidad()),
        decimalStr(m.getStockAnterior()),
        decimalStr(m.getStockResultante()),
        decimalStr(m.getCostoUnitario()),
        m.getMotivo(),
        m.getObservacion(),
        m.getFechaMovimiento()
    );
  }

  private BigDecimal parseDecimal(String raw, String field) {
    try {
      return new BigDecimal(raw.trim());
    } catch (Exception e) {
      throw new BadRequestException("Valor inválido para " + field + ": " + raw);
    }
  }

  private BigDecimal parseDecimalNullable(String raw, BigDecimal fallback) {
    if (raw == null || raw.isBlank()) return fallback;
    return parseDecimal(raw, "decimal");
  }

  private String decimalStr(BigDecimal v) {
    return v == null ? null : v.stripTrailingZeros().toPlainString();
  }

  private String limpiarNullable(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isBlank() ? null : t;
  }

  private String normalizeRequired(String value, String field) {
    if (value == null || value.trim().isBlank()) {
      throw new BadRequestException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }
}