package com.reparasuite.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "taller")
public class Taller {

  @Id
  private Long id;

  @Column(nullable = false)
  private String nombre;

  private String telefono;
  private String email;
  private String direccion;

  @Column(name = "prefijo_ot", nullable = false)
  private String prefijoOt;

  public Long getId() { return id; }
  public String getNombre() { return nombre; }
  public String getTelefono() { return telefono; }
  public String getEmail() { return email; }
  public String getDireccion() { return direccion; }
  public String getPrefijoOt() { return prefijoOt; }

  public void setId(Long id) { this.id = id; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public void setEmail(String email) { this.email = email; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public void setPrefijoOt(String prefijoOt) { this.prefijoOt = prefijoOt; }
}
