package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

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
  @Column(nullable = false, length = 40)
  private EstadoTicket estado;

  @Column(nullable = false, length = 200)
  private String asunto;

  @Column(nullable = false, length = 4000)
  private String descripcion;

  // ✅ vínculo OT
  @Column(name = "orden_trabajo_id", columnDefinition = "uuid")
  private UUID ordenTrabajoId;

  // ✅ snapshot de cliente (para trazabilidad / prefill)
  @Column(name = "cliente_nombre_snapshot", length = 200)
  private String clienteNombreSnapshot;

  @Column(name = "cliente_telefono_snapshot", length = 50)
  private String clienteTelefonoSnapshot;

  @Column(name = "cliente_email_snapshot", length = 200)
  private String clienteEmailSnapshot;

  // ✅ campos estructurados del ticket (MVP fase 1)
  @Column(name = "equipo", length = 200)
  private String equipo;

  @Column(name = "descripcion_falla", length = 4000)
  private String descripcionFalla;

  @Column(name = "tipo_servicio_sugerido", length = 20)
  private String tipoServicioSugerido;

  @Column(name = "direccion", length = 500)
  private String direccion;
  
  @Column(name = "observaciones", length = 4000)
  private String observaciones;

  // 👇 --- NUEVO CAMPO: CATEGORÍAS DE TRABAJO --- 👇
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "ticket_categoria_trabajo", joinColumns = @JoinColumn(name = "ticket_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "categoria")
  private Set<CategoriaTrabajo> categoriasTrabajo = new HashSet<>();
  // 👆 ------------------------------------------ 👆

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

  // Getters
  public UUID getId() { return id; }
  public Cliente getCliente() { return cliente; }
  public EstadoTicket getEstado() { return estado; }
  public String getAsunto() { return asunto; }
  public String getDescripcion() { return descripcion; }
  public UUID getOrdenTrabajoId() { return ordenTrabajoId; }
  public String getClienteNombreSnapshot() { return clienteNombreSnapshot; }
  public String getClienteTelefonoSnapshot() { return clienteTelefonoSnapshot; }
  public String getClienteEmailSnapshot() { return clienteEmailSnapshot; }
  public String getEquipo() { return equipo; }
  public String getDescripcionFalla() { return descripcionFalla; }
  public String getTipoServicioSugerido() { return tipoServicioSugerido; }
  public String getDireccion() { return direccion; }
  public String getObservaciones() { return observaciones; }
  public Set<CategoriaTrabajo> getCategoriasTrabajo() { return categoriasTrabajo; } // <-- Nuevo Getter
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  // Setters
  public void setCliente(Cliente cliente) { this.cliente = cliente; }
  public void setEstado(EstadoTicket estado) { this.estado = estado; }
  public void setAsunto(String asunto) { this.asunto = asunto; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setOrdenTrabajoId(UUID ordenTrabajoId) { this.ordenTrabajoId = ordenTrabajoId; }

  public void setClienteNombreSnapshot(String clienteNombreSnapshot) { this.clienteNombreSnapshot = clienteNombreSnapshot; }
  public void setClienteTelefonoSnapshot(String clienteTelefonoSnapshot) { this.clienteTelefonoSnapshot = clienteTelefonoSnapshot; }
  public void setClienteEmailSnapshot(String clienteEmailSnapshot) { this.clienteEmailSnapshot = clienteEmailSnapshot; }

  public void setEquipo(String equipo) { this.equipo = equipo; }
  public void setDescripcionFalla(String descripcionFalla) { this.descripcionFalla = descripcionFalla; }
  public void setTipoServicioSugerido(String tipoServicioSugerido) { this.tipoServicioSugerido = tipoServicioSugerido; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
  public void setCategoriasTrabajo(Set<CategoriaTrabajo> categoriasTrabajo) { this.categoriasTrabajo = categoriasTrabajo; } // <-- Nuevo Setter

  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}