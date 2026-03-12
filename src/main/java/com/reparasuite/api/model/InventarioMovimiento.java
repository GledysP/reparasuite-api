package com.reparasuite.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(
    name = "inventario_movimiento",
    indexes = {
        @Index(name = "idx_inventario_mov_item", columnList = "inventario_item_id"),
        @Index(name = "idx_inventario_mov_fecha", columnList = "fecha_movimiento")
    }
)
public class InventarioMovimiento {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "inventario_item_id", nullable = false)
  private InventarioItem inventarioItem;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_movimiento", nullable = false, length = 30)
  private TipoMovimientoInventario tipoMovimiento;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal cantidad;

  @Column(name = "stock_anterior", nullable = false, precision = 12, scale = 2)
  private BigDecimal stockAnterior;

  @Column(name = "stock_resultante", nullable = false, precision = 12, scale = 2)
  private BigDecimal stockResultante;

  @Column(name = "costo_unitario", precision = 12, scale = 2)
  private BigDecimal costoUnitario;

  @Column(length = 200)
  private String motivo;

  @Column(length = 4000)
  private String observacion;

  @Column(name = "referencia_tipo", length = 50)
  private String referenciaTipo;

  @Column(name = "referencia_id", columnDefinition = "uuid")
  private UUID referenciaId;

  @Column(name = "usuario_id", columnDefinition = "uuid")
  private UUID usuarioId;

  @Column(name = "fecha_movimiento", nullable = false)
  private OffsetDateTime fechaMovimiento;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (fechaMovimiento == null) fechaMovimiento = OffsetDateTime.now();
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public InventarioItem getInventarioItem() { return inventarioItem; }
  public TipoMovimientoInventario getTipoMovimiento() { return tipoMovimiento; }
  public BigDecimal getCantidad() { return cantidad; }
  public BigDecimal getStockAnterior() { return stockAnterior; }
  public BigDecimal getStockResultante() { return stockResultante; }
  public BigDecimal getCostoUnitario() { return costoUnitario; }
  public String getMotivo() { return motivo; }
  public String getObservacion() { return observacion; }
  public String getReferenciaTipo() { return referenciaTipo; }
  public UUID getReferenciaId() { return referenciaId; }
  public UUID getUsuarioId() { return usuarioId; }
  public OffsetDateTime getFechaMovimiento() { return fechaMovimiento; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void setInventarioItem(InventarioItem inventarioItem) { this.inventarioItem = inventarioItem; }
  public void setTipoMovimiento(TipoMovimientoInventario tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
  public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
  public void setStockAnterior(BigDecimal stockAnterior) { this.stockAnterior = stockAnterior; }
  public void setStockResultante(BigDecimal stockResultante) { this.stockResultante = stockResultante; }
  public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
  public void setMotivo(String motivo) { this.motivo = motivo; }
  public void setObservacion(String observacion) { this.observacion = observacion; }
  public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }
  public void setReferenciaId(UUID referenciaId) { this.referenciaId = referenciaId; }
  public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
  public void setFechaMovimiento(OffsetDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}