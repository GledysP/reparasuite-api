package com.reparasuite.api.service;

import org.springframework.stereotype.Service;

import com.reparasuite.api.dto.TallerDto;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.repo.TallerRepo;

@Service
public class AjustesService {

  private final TallerRepo tallerRepo;

  public AjustesService(TallerRepo tallerRepo) {
    this.tallerRepo = tallerRepo;
  }

  public TallerDto obtenerTaller() {
    Taller t = tallerRepo.findById(1L)
        .orElseThrow(() -> new NotFoundException("Taller no encontrado"));

    return new TallerDto(
        t.getNombre(),
        t.getTelefono(),
        t.getEmail(),
        t.getDireccion(),
        t.getPrefijoOt()
    );
  }

  public void guardarTaller(TallerDto dto) {
    Taller t = tallerRepo.findById(1L)
        .orElseThrow(() -> new NotFoundException("Taller no encontrado"));

    t.setNombre(dto.nombre());
    t.setTelefono(dto.telefono());
    t.setEmail(dto.email());
    t.setDireccion(dto.direccion());

    tallerRepo.save(t);
  }
}