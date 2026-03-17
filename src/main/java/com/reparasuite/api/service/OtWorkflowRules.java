package com.reparasuite.api.service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.model.EstadoOt;

@Component
public class OtWorkflowRules {

  private final Map<EstadoOt, Set<EstadoOt>> allowedTransitions = new EnumMap<>(EstadoOt.class);

  public OtWorkflowRules() {
    allowedTransitions.put(EstadoOt.RECIBIDA,
        EnumSet.of(EstadoOt.PRESUPUESTO, EstadoOt.EN_CURSO));

    allowedTransitions.put(EstadoOt.PRESUPUESTO,
        EnumSet.of(EstadoOt.APROBADA, EstadoOt.RECIBIDA));

    allowedTransitions.put(EstadoOt.APROBADA,
        EnumSet.of(EstadoOt.EN_CURSO, EstadoOt.CERRADA));

    allowedTransitions.put(EstadoOt.EN_CURSO,
        EnumSet.of(EstadoOt.FINALIZADA, EstadoOt.CERRADA));

    allowedTransitions.put(EstadoOt.FINALIZADA,
        EnumSet.of(EstadoOt.CERRADA));

    allowedTransitions.put(EstadoOt.CERRADA,
        EnumSet.noneOf(EstadoOt.class));
  }

  public void validateTransition(EstadoOt current, EstadoOt target) {
    if (current == null || target == null) {
      throw new BadRequestException("Estado inválido");
    }

    if (current == target) {
      return;
    }

    Set<EstadoOt> allowed = allowedTransitions.getOrDefault(current, EnumSet.noneOf(EstadoOt.class));
    if (!allowed.contains(target)) {
      throw new BadRequestException(
          "Transición de estado no permitida: " + current.name() + " -> " + target.name()
      );
    }
  }
}