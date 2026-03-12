package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "inventario_categoria")
public class InventarioCategoria {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String codigo;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(length = 2000)
  private String descripcion;

  @Column(name = "orden_visual", nullable = false)
  private Integer ordenVisual = 0;

  @Column(nullable = false)
  private boolean activa = true;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

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
  public String getCodigo() { return codigo; }
  public String getNombre() { return nombre; }
  public String getDescripcion() { return descripcion; }
  public Integer getOrdenVisual() { return ordenVisual; }
  public boolean isActiva() { return activa; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void setCodigo(String codigo) { this.codigo = codigo; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }
  public void setActiva(boolean activa) { this.activa = activa; }
}