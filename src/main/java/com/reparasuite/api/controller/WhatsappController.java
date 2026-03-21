package com.reparasuite.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reparasuite.api.dto.WhatsappLinkResponse;
import com.reparasuite.api.service.WhatsappService;

@RestController
@RequestMapping("/api/v1/whatsapp")
@PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
public class WhatsappController {

  private final WhatsappService whatsappService;

  public WhatsappController(WhatsappService whatsappService) {
    this.whatsappService = whatsappService;
  }

  @GetMapping("/invitar-registro")
  public ResponseEntity<WhatsappLinkResponse> generarLinkInvitacionRegistro() {
    String url = whatsappService.generarLinkInvitacionRegistro();
    return ResponseEntity.ok(new WhatsappLinkResponse(url));
  }

  @GetMapping("/recordar-login/{clienteId}")
  public ResponseEntity<WhatsappLinkResponse> generarLinkRecordatorioLogin(@PathVariable UUID clienteId) {
    String url = whatsappService.generarLinkRecordatorioLogin(clienteId);
    return ResponseEntity.ok(new WhatsappLinkResponse(url));
  }
}