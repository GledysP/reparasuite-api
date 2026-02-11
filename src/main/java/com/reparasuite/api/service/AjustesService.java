package com.reparasuite.api.service;

import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.TallerDto;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.repo.TallerRepo;

@Service
public class AjustesService {

  private final TallerRepo tallerRepo;

  public AjustesService(TallerRepo tallerRepo) {
    this.tallerRepo = tallerRepo;
  }

  public TallerDto obtenerTaller() {
    Taller t = tallerRepo.findById(1L).orElseThrow();
    return new TallerDto(t.getNombre(), t.getTelefono(), t.getEmail(), t.getDireccion(), t.getPrefijoOt());
  }

  public void guardarTaller(TallerDto dto) {
    Taller t = tallerRepo.findById(1L).orElseThrow();
    t.setNombre(dto.nombre());
    t.setTelefono(dto.telefono());
    t.setEmail(dto.email());
    t.setDireccion(dto.direccion());
    // prefijoOt NO editable en MVP
    tallerRepo.save(t);
  }
}
