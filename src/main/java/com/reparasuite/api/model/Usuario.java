package com.reparasuite.api.model;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false, unique = true)
  private String usuario;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RolUsuario rol;

  @Column(nullable = false)
  private boolean activo;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public String getNombre() { return nombre; }
  public String getUsuario() { return usuario; }
  public String getPasswordHash() { return passwordHash; }
  public RolUsuario getRol() { return rol; }
  public boolean isActivo() { return activo; }

  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setUsuario(String usuario) { this.usuario = usuario; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setRol(RolUsuario rol) { this.rol = rol; }
  public void setActivo(boolean activo) { this.activo = activo; }
}
