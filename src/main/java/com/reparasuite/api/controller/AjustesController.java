package com.reparasuite.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.TallerDto;
import com.reparasuite.api.service.AjustesService;

@RestController
@RequestMapping("/api/v1/ajustes")
public class AjustesController {

  private final AjustesService service;

  public AjustesController(AjustesService service) {
    this.service = service;
  }

  @GetMapping("/taller")
  public TallerDto obtenerTaller() {
    return service.obtenerTaller();
  }

  @PutMapping("/taller")
  public ResponseEntity<?> guardar(@Validated @RequestBody TallerDto dto) {
    service.guardarTaller(dto);
    return ResponseEntity.noContent().build();
  }
}
