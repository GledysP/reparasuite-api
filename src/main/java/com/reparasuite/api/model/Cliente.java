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
  private String email;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public String getNombre() { return nombre; }
  public String getTelefono() { return telefono; }
  public String getEmail() { return email; }

  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public void setEmail(String email) { this.email = email; }
}
