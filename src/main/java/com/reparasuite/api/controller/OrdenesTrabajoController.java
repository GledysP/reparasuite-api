package com.reparasuite.api.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.service.OrdenesTrabajoService;

@RestController
@RequestMapping("/api/v1/ordenes-trabajo")
public class OrdenesTrabajoController {

  private final OrdenesTrabajoService service;

  public OrdenesTrabajoController(OrdenesTrabajoService service) {
    this.service = service;
  }

  // Backoffice list (✅ con filtros)
  @GetMapping
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

  // Shared detail (cliente verá filtrado)
  @GetMapping("/{id}")
  public OtDetalleDto obtener(@PathVariable String id) {
    return service.obtener(id);
  }

  // Backoffice create
  @PostMapping
  public ResponseEntity<?> crear(@Validated @RequestBody OtCrearRequest req) {
    return ResponseEntity.ok(service.crear(req));
  }

  // Backoffice change status
  @PatchMapping("/{id}/estado")
  public ResponseEntity<?> cambiarEstado(@PathVariable String id, @Validated @RequestBody OtCambiarEstadoRequest req) {
    service.cambiarEstado(id, req.estado());
    return ResponseEntity.noContent().build();
  }

  // Notes (cliente puede usarlo; será visibleCliente=true forzado)
  @PostMapping("/{id}/notas")
  public ResponseEntity<?> anadirNota(@PathVariable String id, @Validated @RequestBody OtNotaRequest req,
                                      @RequestParam(defaultValue = "false") boolean visibleCliente) {
    service.anadirNota(id, req.contenido(), visibleCliente);
    return ResponseEntity.noContent().build();
  }

  // Fotos (cliente puede usarlo; será visibleCliente=true forzado)
  @PostMapping("/{id}/fotos")
  public ResponseEntity<FotoDto> subirFoto(@PathVariable String id,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam(defaultValue = "false") boolean visibleCliente) throws IOException {
    return ResponseEntity.ok(service.subirFoto(id, file, visibleCliente));
  }

  // Presupuesto backoffice
  @PostMapping("/{id}/presupuesto")
  public ResponseEntity<PresupuestoDto> guardarPresupuesto(@PathVariable String id, @Validated @RequestBody PresupuestoGuardarRequest req) {
    return ResponseEntity.ok(service.guardarPresupuesto(id, req));
  }

  @PostMapping("/{id}/presupuesto/enviar")
  public ResponseEntity<PresupuestoDto> enviarPresupuesto(@PathVariable String id) {
    return ResponseEntity.ok(service.enviarPresupuesto(id));
  }

  // Presupuesto cliente
  @PostMapping("/{id}/presupuesto/aceptar")
  public ResponseEntity<?> aceptarPresupuesto(@PathVariable String id, @Validated @RequestBody PresupuestoAceptarRequest req) {
    service.aceptarPresupuesto(id, req.acepto());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/presupuesto/rechazar")
  public ResponseEntity<?> rechazarPresupuesto(@PathVariable String id) {
    service.rechazarPresupuesto(id);
    return ResponseEntity.noContent().build();
  }

  // Pago cliente (MVP)
  @PostMapping("/{id}/pago/transferencia")
  public ResponseEntity<?> marcarTransferencia(@PathVariable String id) {
    service.marcarTransferencia(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/pago/comprobante")
  public ResponseEntity<PagoDto> subirComprobante(@PathVariable String id, @RequestParam("file") MultipartFile file) throws IOException {
    return ResponseEntity.ok(service.subirComprobantePago(id, file));
  }

  // ✅ Pago backoffice (confirmar recepción)
  @PostMapping("/{id}/pago/confirmar")
  public ResponseEntity<?> confirmarPagoRecibido(@PathVariable String id) {
    service.confirmarPagoRecibido(id);
    return ResponseEntity.noContent().build();
  }

  // Citas (cliente / backoffice según rol)
  @PostMapping("/{id}/citas")
  public ResponseEntity<CitaDto> reservarCita(@PathVariable String id, @Validated @RequestBody CitaRequest req) {
    return ResponseEntity.ok(service.reservarCita(id, req));
  }

  @PutMapping("/citas/{citaId}")
  public ResponseEntity<CitaDto> reprogramar(@PathVariable String citaId, @Validated @RequestBody CitaRequest req) {
    return ResponseEntity.ok(service.reprogramarCita(UUID.fromString(citaId), req));
  }

  // Mensajería (cliente y backoffice)
  @PostMapping("/{id}/mensajes")
  public ResponseEntity<MensajeDto> enviarMensaje(@PathVariable String id, @Validated @RequestBody MensajeEnviarRequest req) {
    return ResponseEntity.ok(service.enviarMensaje(id, req.contenido()));
  }
}