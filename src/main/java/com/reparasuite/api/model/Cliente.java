package com.reparasuite.api.model;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "cliente")
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String nombre;

  @Column
  private String telefono;

  @Column
  private String email;

  @Column(name = "password_hash_portal")
  private String passwordHashPortal;

  @Column(name = "portal_activo", nullable = false)
  private boolean portalActivo;

  // Constructor vacío necesario para JPA
  public Cliente() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHashPortal() {
    return passwordHashPortal;
  }

  public void setPasswordHashPortal(String passwordHashPortal) {
    this.passwordHashPortal = passwordHashPortal;
  }

  public boolean isPortalActivo() {
    return portalActivo;
  }

  public void setPortalActivo(boolean portalActivo) {
    this.portalActivo = portalActivo;
  }
}