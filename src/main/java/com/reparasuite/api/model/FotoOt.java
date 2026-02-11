package com.reparasuite.api.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "foto_ot")
public class FotoOt {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "ot_id")
  private OrdenTrabajo ot;

  @Column(nullable = false, length = 500)
  private String url;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public UUID getId() { return id; }
  public OrdenTrabajo getOt() { return ot; }
  public String getUrl() { return url; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void setOt(OrdenTrabajo ot) { this.ot = ot; }
  public void setUrl(String url) { this.url = url; }
}
