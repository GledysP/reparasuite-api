package com.reparasuite.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(
    name = "inventario_item",
    indexes = {
        @Index(name = "idx_inventario_item_categoria", columnList = "categoria_id"),
        @Index(name = "idx_inventario_item_nombre", columnList = "nombre"),
        @Index(name = "idx_inventario_item_sku", columnList = "sku")
    }
)
public class InventarioItem {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String sku;

  @Column(name = "codigo_barras", length = 100)
  private String codigoBarras;

  @Column(nullable = false, length = 200)
  private String nombre;

  @Column(length = 4000)
  private String descripcion;

  @ManyToOne
  @JoinColumn(name = "categoria_id")
  private InventarioCategoria categoria;

  @Column(length = 120)
  private String marca;

  @Column(name = "modelo_compatibilidad", length = 200)
  private String modeloCompatibilidad;

  @Enumerated(EnumType.STRING)
  @Column(name = "unidad_medida", nullable = false, length = 20)
  private UnidadMedidaInventario unidadMedida = UnidadMedidaInventario.UNIDAD;

  @Column(name = "stock_actual", nullable = false, precision = 12, scale = 2)
  private BigDecimal stockActual = BigDecimal.ZERO;

  @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 2)
  private BigDecimal stockMinimo = BigDecimal.ZERO;

  @Column(name = "stock_maximo", precision = 12, scale = 2)
  private BigDecimal stockMaximo;

  @Column(name = "controla_stock", nullable = false)
  private boolean controlaStock = true;

  @Column(name = "permite_stock_negativo", nullable = false)
  private boolean permiteStockNegativo = false;

  @Column(name = "costo_promedio", nullable = false, precision = 12, scale = 2)
  private BigDecimal costoPromedio = BigDecimal.ZERO;

  @Column(name = "ultimo_costo", nullable = false, precision = 12, scale = 2)
  private BigDecimal ultimoCosto = BigDecimal.ZERO;

  @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
  private BigDecimal precioVenta = BigDecimal.ZERO;

  @Column(name = "ubicacion_almacen", length = 120)
  private String ubicacionAlmacen;

  @Column(length = 4000)
  private String notas;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "created_by", columnDefinition = "uuid")
  private UUID createdBy;

  @Column(name = "updated_by", columnDefinition = "uuid")
  private UUID updatedBy;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public String getSku() { return sku; }
  public String getCodigoBarras() { return codigoBarras; }
  public String getNombre() { return nombre; }
  public String getDescripcion() { return descripcion; }
  public InventarioCategoria getCategoria() { return categoria; }
  public String getMarca() { return marca; }
  public String getModeloCompatibilidad() { return modeloCompatibilidad; }
  public UnidadMedidaInventario getUnidadMedida() { return unidadMedida; }
  public BigDecimal getStockActual() { return stockActual; }
  public BigDecimal getStockMinimo() { return stockMinimo; }
  public BigDecimal getStockMaximo() { return stockMaximo; }
  public boolean isControlaStock() { return controlaStock; }
  public boolean isPermiteStockNegativo() { return permiteStockNegativo; }
  public BigDecimal getCostoPromedio() { return costoPromedio; }
  public BigDecimal getUltimoCosto() { return ultimoCosto; }
  public BigDecimal getPrecioVenta() { return precioVenta; }
  public String getUbicacionAlmacen() { return ubicacionAlmacen; }
  public String getNotas() { return notas; }
  public boolean isActivo() { return activo; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public UUID getCreatedBy() { return createdBy; }
  public UUID getUpdatedBy() { return updatedBy; }

  public void setSku(String sku) { this.sku = sku; }
  public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setCategoria(InventarioCategoria categoria) { this.categoria = categoria; }
  public void setMarca(String marca) { this.marca = marca; }
  public void setModeloCompatibilidad(String modeloCompatibilidad) { this.modeloCompatibilidad = modeloCompatibilidad; }
  public void setUnidadMedida(UnidadMedidaInventario unidadMedida) { this.unidadMedida = unidadMedida; }
  public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }
  public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }
  public void setStockMaximo(BigDecimal stockMaximo) { this.stockMaximo = stockMaximo; }
  public void setControlaStock(boolean controlaStock) { this.controlaStock = controlaStock; }
  public void setPermiteStockNegativo(boolean permiteStockNegativo) { this.permiteStockNegativo = permiteStockNegativo; }
  public void setCostoPromedio(BigDecimal costoPromedio) { this.costoPromedio = costoPromedio; }
  public void setUltimoCosto(BigDecimal ultimoCosto) { this.ultimoCosto = ultimoCosto; }
  public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
  public void setUbicacionAlmacen(String ubicacionAlmacen) { this.ubicacionAlmacen = ubicacionAlmacen; }
  public void setNotas(String notas) { this.notas = notas; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
  public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}