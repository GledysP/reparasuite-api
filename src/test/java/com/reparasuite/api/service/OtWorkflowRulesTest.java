package com.reparasuite.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.model.EstadoOt;

class OtWorkflowRulesTest {

  private final OtWorkflowRules rules = new OtWorkflowRules();

  @Test
  void debePermitirTransicionValida() {
    assertDoesNotThrow(() ->
        rules.validateTransition(EstadoOt.RECIBIDA, EstadoOt.PRESUPUESTO)
    );
  }

  @Test
  void debePermitirMismoEstado() {
    assertDoesNotThrow(() ->
        rules.validateTransition(EstadoOt.EN_CURSO, EstadoOt.EN_CURSO)
    );
  }

  @Test
  void debeRechazarTransicionInvalida() {
    assertThrows(BadRequestException.class, () ->
        rules.validateTransition(EstadoOt.RECIBIDA, EstadoOt.FINALIZADA)
    );
  }

  @Test
  void debeRechazarDestinoNulo() {
    assertThrows(BadRequestException.class, () ->
        rules.validateTransition(EstadoOt.RECIBIDA, null)
    );
  }
}