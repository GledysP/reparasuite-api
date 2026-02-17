package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "historial_ot")
public class HistorialOt {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "ot_id")
  private OrdenTrabajo ot;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventoHistorialOt evento;

  @Column(nullable = false, length = 2000)
  private String descripcion;

  @Column(name = "fecha", nullable = false)
  private OffsetDateTime fecha;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_tipo", nullable = false)
  private ActorTipo actorTipo;

  @Column(name = "actor_nombre", nullable = false, length = 200)
  private String actorNombre;

  // (opcional) link a Usuario si aplica
  @ManyToOne
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (fecha == null) fecha = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public OrdenTrabajo getOt() { return ot; }
  public EventoHistorialOt getEvento() { return evento; }
  public String getDescripcion() { return descripcion; }
  public OffsetDateTime getFecha() { return fecha; }
  public ActorTipo getActorTipo() { return actorTipo; }
  public String getActorNombre() { return actorNombre; }
  public Usuario getUsuario() { return usuario; }

  public void setOt(OrdenTrabajo ot) { this.ot = ot; }
  public void setEvento(EventoHistorialOt evento) { this.evento = evento; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
  public void setActorTipo(ActorTipo actorTipo) { this.actorTipo = actorTipo; }
  public void setActorNombre(String actorNombre) { this.actorNombre = actorNombre; }
  public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
