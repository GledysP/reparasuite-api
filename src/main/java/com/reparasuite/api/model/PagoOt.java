package com.reparasuite.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "pago_ot")
public class PagoOt {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @OneToOne(optional = false)
  @JoinColumn(name = "ot_id", unique = true)
  private OrdenTrabajo ot;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoPagoOt estado;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal importe;

  @Column(name = "comprobante_url", length = 500)
  private String comprobanteUrl;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
    if (estado == null) estado = EstadoPagoOt.PENDIENTE;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public OrdenTrabajo getOt() { return ot; }
  public EstadoPagoOt getEstado() { return estado; }
  public BigDecimal getImporte() { return importe; }
  public String getComprobanteUrl() { return comprobanteUrl; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void setOt(OrdenTrabajo ot) { this.ot = ot; }
  public void setEstado(EstadoPagoOt estado) { this.estado = estado; }
  public void setImporte(BigDecimal importe) { this.importe = importe; }
  public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }
}
