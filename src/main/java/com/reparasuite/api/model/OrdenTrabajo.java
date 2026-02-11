package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "orden_trabajo")
public class OrdenTrabajo {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String codigo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoOt estado;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoOt tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PrioridadOt prioridad;

  @Column(nullable = false, length = 4000)
  private String descripcion;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "tecnico_id")
  private Usuario tecnico;

  private OffsetDateTime fechaPrevista;

  private String direccion;

  @Column(name = "notas_acceso", length = 2000)
  private String notasAcceso;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
    if (estado == null) estado = EstadoOt.RECIBIDA;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public String getCodigo() { return codigo; }
  public EstadoOt getEstado() { return estado; }
  public TipoOt getTipo() { return tipo; }
  public PrioridadOt getPrioridad() { return prioridad; }
  public String getDescripcion() { return descripcion; }
  public Cliente getCliente() { return cliente; }
  public Usuario getTecnico() { return tecnico; }
  public OffsetDateTime getFechaPrevista() { return fechaPrevista; }
  public String getDireccion() { return direccion; }
  public String getNotasAcceso() { return notasAcceso; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void setCodigo(String codigo) { this.codigo = codigo; }
  public void setEstado(EstadoOt estado) { this.estado = estado; }
  public void setTipo(TipoOt tipo) { this.tipo = tipo; }
  public void setPrioridad(PrioridadOt prioridad) { this.prioridad = prioridad; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setCliente(Cliente cliente) { this.cliente = cliente; }
  public void setTecnico(Usuario tecnico) { this.tecnico = tecnico; }
  public void setFechaPrevista(OffsetDateTime fechaPrevista) { this.fechaPrevista = fechaPrevista; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public void setNotasAcceso(String notasAcceso) { this.notasAcceso = notasAcceso; }
}
