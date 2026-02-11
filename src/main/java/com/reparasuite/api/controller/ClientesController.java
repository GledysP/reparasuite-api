package com.reparasuite.api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.ClientesService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClientesController {

  private final ClientesService service;

  public ClientesController(ClientesService service) {
    this.service = service;
  }

  @GetMapping
  public ApiListaResponse<ClienteResumenDto> listar(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(query, page, size);
  }

  @GetMapping("/{id}")
  public ClienteResumenDto obtener(@PathVariable String id) {
    return service.obtener(UUID.fromString(id));
  }
}
