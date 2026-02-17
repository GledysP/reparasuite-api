package com.reparasuite.api.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "presupuesto_ot")
public class PresupuestoOt {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @OneToOne(optional = false)
  @JoinColumn(name = "ot_id", unique = true)
  private OrdenTrabajo ot;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoPresupuesto estado;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal importe;

  @Column(nullable = false, length = 4000)
  private String detalle;

  // ✅ check aceptación (firma “checkbox”)
  @Column(name = "aceptacion_check", nullable = false)
  private boolean aceptacionCheck = false;

  @Column(name = "aceptacion_at")
  private OffsetDateTime aceptacionAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "sent_at")
  private OffsetDateTime sentAt;

  @Column(name = "responded_at")
  private OffsetDateTime respondedAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
    if (estado == null) estado = EstadoPresupuesto.BORRADOR;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public OrdenTrabajo getOt() { return ot; }
  public EstadoPresupuesto getEstado() { return estado; }
  public BigDecimal getImporte() { return importe; }
  public String getDetalle() { return detalle; }
  public boolean isAceptacionCheck() { return aceptacionCheck; }
  public OffsetDateTime getAceptacionAt() { return aceptacionAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public OffsetDateTime getSentAt() { return sentAt; }
  public OffsetDateTime getRespondedAt() { return respondedAt; }

  public void setOt(OrdenTrabajo ot) { this.ot = ot; }
  public void setEstado(EstadoPresupuesto estado) { this.estado = estado; }
  public void setImporte(BigDecimal importe) { this.importe = importe; }
  public void setDetalle(String detalle) { this.detalle = detalle; }
  public void setAceptacionCheck(boolean aceptacionCheck) { this.aceptacionCheck = aceptacionCheck; }
  public void setAceptacionAt(OffsetDateTime aceptacionAt) { this.aceptacionAt = aceptacionAt; }
  public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
  public void setRespondedAt(OffsetDateTime respondedAt) { this.respondedAt = respondedAt; }
}
