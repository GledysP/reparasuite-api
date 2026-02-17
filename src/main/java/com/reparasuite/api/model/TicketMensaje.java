package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_mensaje")
public class TicketMensaje {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "ticket_id")
  private TicketSolicitud ticket;

  @Enumerated(EnumType.STRING)
  @Column(name = "remitente_tipo", nullable = false)
  private TipoRemitente remitenteTipo;

  @Column(name = "remitente_nombre", nullable = false, length = 200)
  private String remitenteNombre;

  @Column(nullable = false, length = 2000)
  private String contenido;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public TicketSolicitud getTicket() { return ticket; }
  public TipoRemitente getRemitenteTipo() { return remitenteTipo; }
  public String getRemitenteNombre() { return remitenteNombre; }
  public String getContenido() { return contenido; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void setTicket(TicketSolicitud ticket) { this.ticket = ticket; }
  public void setRemitenteTipo(TipoRemitente remitenteTipo) { this.remitenteTipo = remitenteTipo; }
  public void setRemitenteNombre(String remitenteNombre) { this.remitenteNombre = remitenteNombre; }
  public void setContenido(String contenido) { this.contenido = contenido; }
}
