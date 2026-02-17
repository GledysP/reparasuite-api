package com.reparasuite.api.model;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String nombre;

  private String telefono;

  @Column(unique = true)
  private String email;

  // ✅ Portal
  @Column(name = "portal_activo", nullable = false)
  private boolean portalActivo = false;

  @Column(name = "password_hash_portal")
  private String passwordHashPortal;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public String getNombre() { return nombre; }
  public String getTelefono() { return telefono; }
  public String getEmail() { return email; }
  public boolean isPortalActivo() { return portalActivo; }
  public String getPasswordHashPortal() { return passwordHashPortal; }

  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public void setEmail(String email) { this.email = email; }
  public void setPortalActivo(boolean portalActivo) { this.portalActivo = portalActivo; }
  public void setPasswordHashPortal(String passwordHashPortal) { this.passwordHashPortal = passwordHashPortal; }
}
