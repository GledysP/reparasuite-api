package com.reparasuite.api.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.CitaDto;
import com.reparasuite.api.dto.CitaRequest;
import com.reparasuite.api.dto.FotoDto;
import com.reparasuite.api.dto.MensajeDto;
import com.reparasuite.api.dto.MensajeEnviarRequest;
import com.reparasuite.api.dto.OtCambiarEstadoRequest;
import com.reparasuite.api.dto.OtCrearRequest;
import com.reparasuite.api.dto.OtDetalleDto;
import com.reparasuite.api.dto.OtListaItemDto;
import com.reparasuite.api.dto.OtNotaRequest;
import com.reparasuite.api.dto.OtRevisionTecnicaRequest;
import com.reparasuite.api.dto.PagoDto;
import com.reparasuite.api.dto.PresupuestoAceptarRequest;
import com.reparasuite.api.dto.PresupuestoDto;
import com.reparasuite.api.dto.PresupuestoGuardarRequest;
import com.reparasuite.api.service.OrdenesTrabajoService;

@RestController
@RequestMapping("/api/v1/ordenes-trabajo")
public class OrdenesTrabajoController {

  private final OrdenesTrabajoService service;

  public OrdenesTrabajoController(OrdenesTrabajoService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ApiListaResponse<OtListaItemDto> listar(
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) String prioridad,
      @RequestParam(required = false) String tecnicoId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.listar(query, estado, tipo, prioridad, tecnicoId, page, size);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public OtDetalleDto obtener(@PathVariable String id) {
    return service.obtener(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<?> crear(@Validated @RequestBody OtCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<?> eliminar(@PathVariable String id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<?> cambiarEstado(
      @PathVariable String id,
      @Validated @RequestBody OtCambiarEstadoRequest req
  ) {
    service.cambiarEstado(id, req.estado());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/notas")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<?> anadirNota(
      @PathVariable String id,
      @Validated @RequestBody OtNotaRequest req,
      @RequestParam(defaultValue = "false") boolean visibleCliente
  ) {
    service.anadirNota(id, req.contenido(), visibleCliente);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/fotos")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<FotoDto> subirFoto(
      @PathVariable String id,
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "false") boolean visibleCliente
  ) throws IOException {
    return ResponseEntity.ok(service.subirFoto(id, file, visibleCliente));
  }

  @PostMapping("/{id}/presupuesto")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PresupuestoDto> guardarPresupuesto(
      @PathVariable String id,
      @Validated @RequestBody PresupuestoGuardarRequest req
  ) {
    return ResponseEntity.ok(service.guardarPresupuesto(id, req));
  }

  @PostMapping("/{id}/presupuesto/enviar")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PresupuestoDto> enviarPresupuesto(@PathVariable String id) {
    return ResponseEntity.ok(service.enviarPresupuesto(id));
  }

  @PostMapping("/{id}/presupuesto/aceptar")
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<?> aceptarPresupuesto(
      @PathVariable String id,
      @Validated @RequestBody PresupuestoAceptarRequest req
  ) {
    service.aceptarPresupuesto(id, req.acepto());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/presupuesto/rechazar")
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<?> rechazarPresupuesto(@PathVariable String id) {
    service.rechazarPresupuesto(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/pago/transferencia")
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<?> marcarTransferencia(@PathVariable String id) {
    service.marcarTransferencia(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/pago/comprobante")
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<PagoDto> subirComprobante(
      @PathVariable String id,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    return ResponseEntity.ok(service.subirComprobantePago(id, file));
  }

  @PostMapping("/{id}/pago/confirmar")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<?> confirmarPagoRecibido(@PathVariable String id) {
    service.confirmarPagoRecibido(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/citas")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<CitaDto> reservarCita(
      @PathVariable String id,
      @Validated @RequestBody CitaRequest req
  ) {
    return ResponseEntity.ok(service.reservarCita(id, req));
  }

  @PutMapping("/citas/{citaId}")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<CitaDto> reprogramar(
      @PathVariable String citaId,
      @Validated @RequestBody CitaRequest req
  ) {
    return ResponseEntity.ok(service.reprogramarCita(UUID.fromString(citaId), req));
  }

  @PostMapping("/{id}/mensajes")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<MensajeDto> enviarMensaje(
      @PathVariable String id,
      @Validated @RequestBody MensajeEnviarRequest req
  ) {
    return ResponseEntity.ok(service.enviarMensaje(id, req.contenido()));
  }

  @PatchMapping("/{id}/revision-tecnica")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<OtDetalleDto> actualizarRevisionTecnica(
      @PathVariable String id,
      @RequestBody OtRevisionTecnicaRequest req
  ) {
    return ResponseEntity.ok(service.actualizarRevisionTecnica(id, req));
  }
}