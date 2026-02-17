package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "cita_ot")
public class CitaOt {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "ot_id")
  private OrdenTrabajo ot;

  @Column(name = "inicio", nullable = false)
  private OffsetDateTime inicio;

  @Column(name = "fin", nullable = false)
  private OffsetDateTime fin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoCita estado;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (estado == null) estado = EstadoCita.PROGRAMADA;
  }

  public UUID getId() { return id; }
  public OrdenTrabajo getOt() { return ot; }
  public OffsetDateTime getInicio() { return inicio; }
  public OffsetDateTime getFin() { return fin; }
  public EstadoCita getEstado() { return estado; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void setOt(OrdenTrabajo ot) { this.ot = ot; }
  public void setInicio(OffsetDateTime inicio) { this.inicio = inicio; }
  public void setFin(OffsetDateTime fin) { this.fin = fin; }
  public void setEstado(EstadoCita estado) { this.estado = estado; }
}
