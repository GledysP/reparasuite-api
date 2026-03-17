package com.reparasuite.api.service;

import org.springframework.stereotype.Service;

import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.OrdenTrabajo;

@Service
public class OrdenTrabajoWorkflowService {

  private final OtWorkflowRules workflowRules;

  public OrdenTrabajoWorkflowService(OtWorkflowRules workflowRules) {
    this.workflowRules = workflowRules;
  }

  public void moveTo(OrdenTrabajo ot, EstadoOt target) {
    workflowRules.validateTransition(ot.getEstado(), target);
    ot.setEstado(target);
  }
}