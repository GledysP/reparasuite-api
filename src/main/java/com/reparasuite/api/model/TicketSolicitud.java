package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_solicitud")
public class TicketSolicitud {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id")
  private Cliente cliente;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoTicket estado;

  @Column(nullable = false, length = 200)
  private String asunto;

  @Column(nullable = false, length = 4000)
  private String descripcion;

  // ✅ NUEVO: OT creada a partir del ticket (preorden -> OT)
  @Column(name = "orden_trabajo_id", columnDefinition = "uuid")
  private UUID ordenTrabajoId;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
    if (estado == null) estado = EstadoTicket.ABIERTO;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public Cliente getCliente() { return cliente; }
  public EstadoTicket getEstado() { return estado; }
  public String getAsunto() { return asunto; }
  public String getDescripcion() { return descripcion; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public UUID getOrdenTrabajoId() { return ordenTrabajoId; }
  public void setOrdenTrabajoId(UUID ordenTrabajoId) { this.ordenTrabajoId = ordenTrabajoId; }

  public void setCliente(Cliente cliente) { this.cliente = cliente; }
  public void setEstado(EstadoTicket estado) { this.estado = estado; }
  public void setAsunto(String asunto) { this.asunto = asunto; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
