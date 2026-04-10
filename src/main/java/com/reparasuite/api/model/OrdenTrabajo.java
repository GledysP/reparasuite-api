package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

import jakarta.persistence.*;

@Entity
@Table(
    name = "orden_trabajo",
    indexes = {
        @Index(name = "idx_ot_cliente", columnList = "cliente_id"),
        @Index(name = "idx_ot_tecnico", columnList = "tecnico_id"),
        @Index(name = "idx_ot_equipo", columnList = "equipo_id"),
        @Index(name = "idx_ot_categoria_equipo", columnList = "categoria_equipo_id")
    }
)
public class OrdenTrabajo {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
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

  @Column(length = 255)
  private String equipo;

  @ManyToOne
  @JoinColumn(name = "equipo_id")
  private Equipo equipoRegistrado;

  @ManyToOne
  @JoinColumn(name = "categoria_equipo_id")
  private CategoriaEquipo categoriaEquipo;

  @Column(name = "falla_reportada", length = 1000)
  private String fallaReportada;

  @Column(nullable = false, length = 4000)
  private String descripcion;

  @Column(name = "falla_detectada", length = 1000)
  private String fallaDetectada;

  @Column(name = "diagnostico_tecnico", length = 4000)
  private String diagnosticoTecnico;

  @Column(name = "trabajo_a_realizar", length = 4000)
  private String trabajoARealizar;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "tecnico_id")
  private Usuario tecnico;

  @Column(name = "fecha_prevista")
  private OffsetDateTime fechaPrevista;

  @Column(length = 1000)
  private String direccion;

  @Column(name = "notas_acceso", length = 2000)
  private String notasAcceso;

  // 👇 --- NUEVO CAMPO: CATEGORÍAS DE TRABAJO --- 👇
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "ot_categoria_trabajo", joinColumns = @JoinColumn(name = "ot_id"))
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
    if (estado == null) estado = EstadoOt.RECIBIDA;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  // Getters
  public UUID getId() { return id; }
  public String getCodigo() { return codigo; }
  public EstadoOt getEstado() { return estado; }
  public TipoOt getTipo() { return tipo; }
  public PrioridadOt getPrioridad() { return prioridad; }
  public String getEquipo() { return equipo; }
  public Equipo getEquipoRegistrado() { return equipoRegistrado; }
  public CategoriaEquipo getCategoriaEquipo() { return categoriaEquipo; }
  public String getFallaReportada() { return fallaReportada; }
  public String getDescripcion() { return descripcion; }
  public String getFallaDetectada() { return fallaDetectada; }
  public String getDiagnosticoTecnico() { return diagnosticoTecnico; }
  public String getTrabajoARealizar() { return trabajoARealizar; }
  public Cliente getCliente() { return cliente; }
  public Usuario getTecnico() { return tecnico; }
  public OffsetDateTime getFechaPrevista() { return fechaPrevista; }
  public String getDireccion() { return direccion; }
  public String getNotasAcceso() { return notasAcceso; }
  public Set<CategoriaTrabajo> getCategoriasTrabajo() { return categoriasTrabajo; } // <-- Nuevo Getter
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  // Setters
  public void setCodigo(String codigo) { this.codigo = codigo; }
  public void setEstado(EstadoOt estado) { this.estado = estado; }
  public void setTipo(TipoOt tipo) { this.tipo = tipo; }
  public void setPrioridad(PrioridadOt prioridad) { this.prioridad = prioridad; }
  public void setEquipo(String equipo) { this.equipo = equipo; }
  public void setEquipoRegistrado(Equipo equipoRegistrado) { this.equipoRegistrado = equipoRegistrado; }
  public void setCategoriaEquipo(CategoriaEquipo categoriaEquipo) { this.categoriaEquipo = categoriaEquipo; }
  public void setFallaReportada(String fallaReportada) { this.fallaReportada = fallaReportada; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setFallaDetectada(String fallaDetectada) { this.fallaDetectada = fallaDetectada; }
  public void setDiagnosticoTecnico(String diagnosticoTecnico) { this.diagnosticoTecnico = diagnosticoTecnico; }
  public void setTrabajoARealizar(String trabajoARealizar) { this.trabajoARealizar = trabajoARealizar; }
  public void setCliente(Cliente cliente) { this.cliente = cliente; }
  public void setTecnico(Usuario tecnico) { this.tecnico = tecnico; }
  public void setFechaPrevista(OffsetDateTime fechaPrevista) { this.fechaPrevista = fechaPrevista; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public void setNotasAcceso(String notasAcceso) { this.notasAcceso = notasAcceso; }
  public void setCategoriasTrabajo(Set<CategoriaTrabajo> categoriasTrabajo) { this.categoriasTrabajo = categoriasTrabajo; } // <-- Nuevo Setter
}