package com.reparasuite.api.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.ClienteOtItemDto;
import com.reparasuite.api.service.ClientesService;

@RestController
@RequestMapping("/api/v1/portal/ordenes-trabajo")
@PreAuthorize("hasRole('CLIENTE')")
public class PortalOrdenesController {

  private final ClientesService clientesService;

  public PortalOrdenesController(ClientesService clientesService) {
    this.clientesService = clientesService;
  }

  @GetMapping
  public ApiListaResponse<ClienteOtItemDto> listarMisOrdenesTrabajo(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      JwtAuthenticationToken auth
  ) {
    UUID clienteId = UUID.fromString(auth.getToken().getSubject());
    return clientesService.ordenesTrabajo(clienteId, page, size);
  }
}