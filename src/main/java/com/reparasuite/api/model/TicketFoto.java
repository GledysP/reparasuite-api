package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_foto")
public class TicketFoto {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id", nullable = false)
  private TicketSolicitud ticket;

  @Column(nullable = false, length = 500)
  private String url;

  @Column(name = "nombre_original", length = 255)
  private String nombreOriginal;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public TicketSolicitud getTicket() { return ticket; }
  public String getUrl() { return url; }
  public String getNombreOriginal() { return nombreOriginal; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void setTicket(TicketSolicitud ticket) { this.ticket = ticket; }
  public void setUrl(String url) { this.url = url; }
  public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
}