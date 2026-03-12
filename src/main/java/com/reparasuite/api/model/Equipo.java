package com.reparasuite.api.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(
    name = "equipo",
    indexes = {
        @Index(name = "idx_equipo_cliente", columnList = "cliente_id"),
        @Index(name = "idx_equipo_categoria", columnList = "categoria_equipo_id"),
        @Index(name = "idx_equipo_numero_serie", columnList = "numero_serie")
    }
)
public class Equipo {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "categoria_equipo_id")
  private CategoriaEquipo categoriaEquipo;

  @Column(name = "codigo_equipo", nullable = false, unique = true, length = 50)
  private String codigoEquipo;

  @Column(name = "codigo_interno", length = 50)
  private String codigoInterno;

  @Column(name = "tipo_equipo", length = 120)
  private String tipoEquipo;

  @Column(length = 120)
  private String marca;

  @Column(length = 120)
  private String modelo;

  @Column(name = "numero_serie", length = 120)
  private String numeroSerie;

  @Column(name = "descripcion_general", length = 4000)
  private String descripcionGeneral;

  @Column(name = "fecha_compra")
  private LocalDate fechaCompra;

  @Column(name = "garantia_hasta")
  private LocalDate garantiaHasta;

  @Column(name = "ubicacion_habitual", length = 255)
  private String ubicacionHabitual;

  @Column(name = "notas_tecnicas", length = 4000)
  private String notasTecnicas;

  @Column(name = "estado_activo", nullable = false)
  private boolean estadoActivo = true;

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
  public Cliente getCliente() { return cliente; }
  public CategoriaEquipo getCategoriaEquipo() { return categoriaEquipo; }
  public String getCodigoEquipo() { return codigoEquipo; }
  public String getCodigoInterno() { return codigoInterno; }
  public String getTipoEquipo() { return tipoEquipo; }
  public String getMarca() { return marca; }
  public String getModelo() { return modelo; }
  public String getNumeroSerie() { return numeroSerie; }
  public String getDescripcionGeneral() { return descripcionGeneral; }
  public LocalDate getFechaCompra() { return fechaCompra; }
  public LocalDate getGarantiaHasta() { return garantiaHasta; }
  public String getUbicacionHabitual() { return ubicacionHabitual; }
  public String getNotasTecnicas() { return notasTecnicas; }
  public boolean isEstadoActivo() { return estadoActivo; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public UUID getCreatedBy() { return createdBy; }
  public UUID getUpdatedBy() { return updatedBy; }

  public void setCliente(Cliente cliente) { this.cliente = cliente; }
  public void setCategoriaEquipo(CategoriaEquipo categoriaEquipo) { this.categoriaEquipo = categoriaEquipo; }
  public void setCodigoEquipo(String codigoEquipo) { this.codigoEquipo = codigoEquipo; }
  public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }
  public void setTipoEquipo(String tipoEquipo) { this.tipoEquipo = tipoEquipo; }
  public void setMarca(String marca) { this.marca = marca; }
  public void setModelo(String modelo) { this.modelo = modelo; }
  public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
  public void setDescripcionGeneral(String descripcionGeneral) { this.descripcionGeneral = descripcionGeneral; }
  public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }
  public void setGarantiaHasta(LocalDate garantiaHasta) { this.garantiaHasta = garantiaHasta; }
  public void setUbicacionHabitual(String ubicacionHabitual) { this.ubicacionHabitual = ubicacionHabitual; }
  public void setNotasTecnicas(String notasTecnicas) { this.notasTecnicas = notasTecnicas; }
  public void setEstadoActivo(boolean estadoActivo) { this.estadoActivo = estadoActivo; }
  public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
  public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}