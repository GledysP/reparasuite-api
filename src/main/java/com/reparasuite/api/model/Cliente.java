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

  @Column
  private String telefono;

  @Column(unique = true)
  private String email;

  @Column(name = "password_hash_portal")
  private String passwordHashPortal;

  @Column(name = "portal_activo", nullable = false)
  private boolean portalActivo = false;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public String getNombre() { return nombre; }
  public String getTelefono() { return telefono; }
  public String getEmail() { return email; }
  public String getPasswordHashPortal() { return passwordHashPortal; }
  public boolean isPortalActivo() { return portalActivo; }

  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public void setEmail(String email) { this.email = email; }
  public void setPasswordHashPortal(String passwordHashPortal) { this.passwordHashPortal = passwordHashPortal; }
  public void setPortalActivo(boolean portalActivo) { this.portalActivo = portalActivo; }
}