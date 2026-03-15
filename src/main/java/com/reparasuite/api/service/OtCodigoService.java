package com.reparasuite.api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.reparasuite.api.repo.OrdenTrabajoRepo;

@Service
public class OtCodigoService {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OrdenTrabajoRepo ordenTrabajoRepo;

  public OtCodigoService(OrdenTrabajoRepo ordenTrabajoRepo) {
    this.ordenTrabajoRepo = ordenTrabajoRepo;
  }

  public synchronized String generarCodigo(String prefijo) {
    String safePrefijo = sanitizePrefijo(prefijo);
    String base = safePrefijo + "-" + LocalDate.now().format(DATE_FMT);

    for (int intento = 1; intento <= 9999; intento++) {
      String codigo = base + "-" + String.format("%04d", intento);
      if (ordenTrabajoRepo.findByCodigoIgnoreCase(codigo).isEmpty()) {
        return codigo;
      }
    }

    throw new IllegalStateException("No fue posible generar un código único para la OT");
  }

  private String sanitizePrefijo(String prefijo) {
    if (prefijo == null || prefijo.isBlank()) {
      return "OT";
    }

    String out = prefijo.trim().toUpperCase().replaceAll("[^A-Z0-9_-]", "");
    return out.isBlank() ? "OT" : out;
  }
}