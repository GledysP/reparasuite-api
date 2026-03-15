package com.reparasuite.api.controller;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.repo.FotoOtRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.TicketFotoRepo;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ArchivoController {

  private final FotoOtRepo fotoOtRepo;
  private final TicketFotoRepo ticketFotoRepo;
  private final PagoOtRepo pagoOtRepo;

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public ArchivoController(
      FotoOtRepo fotoOtRepo,
      TicketFotoRepo ticketFotoRepo,
      PagoOtRepo pagoOtRepo
  ) {
    this.fotoOtRepo = fotoOtRepo;
    this.ticketFotoRepo = ticketFotoRepo;
    this.pagoOtRepo = pagoOtRepo;
  }

  @GetMapping("/files/**")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
  public ResponseEntity<Resource> descargar(
      HttpServletRequest request,
      JwtAuthenticationToken auth
  ) throws Exception {

    String relativePath = extractRelativePath(request);
    if (relativePath == null || relativePath.isBlank()) {
      throw new IllegalArgumentException("Ruta de archivo inválida");
    }

    String url = "/files/" + relativePath.replace("\\", "/");

    authorizeAccess(url, auth);

    Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
    Path resolved = basePath.resolve(relativePath).normalize();

    if (!resolved.startsWith(basePath)) {
      throw new SecurityException("Ruta de archivo no permitida");
    }

    if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
      throw new RuntimeException("Archivo no encontrado");
    }

    Resource resource = toResource(resolved);
    String contentType = Files.probeContentType(resolved);
    if (contentType == null || contentType.isBlank()) {
      contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .body(resource);
  }

  private void authorizeAccess(String url, JwtAuthenticationToken auth) {
    var fotoOt = fotoOtRepo.findByUrl(url).orElse(null);
    if (fotoOt != null) {
      authorizeOt(fotoOt.getOt(), auth);
      return;
    }

    var ticketFoto = ticketFotoRepo.findByUrl(url).orElse(null);
    if (ticketFoto != null) {
      authorizeTicket(ticketFoto.getTicket(), auth);
      return;
    }

    var pago = pagoOtRepo.findByComprobanteUrl(url).orElse(null);
    if (pago != null) {
      authorizeOt(pago.getOt(), auth);
      return;
    }

    throw new RuntimeException("Archivo no encontrado");
  }

  private void authorizeOt(OrdenTrabajo ot, JwtAuthenticationToken auth) {
    String rol = getRol(auth);

    if (isBackoffice(rol)) {
      return;
    }

    if ("CLIENTE".equalsIgnoreCase(rol)) {
      UUID clienteId = UUID.fromString(auth.getToken().getSubject());
      if (ot.getCliente() == null || !clienteId.equals(ot.getCliente().getId())) {
        throw new SecurityException("No autorizado");
      }
      return;
    }

    throw new SecurityException("No autorizado");
  }

  private void authorizeTicket(TicketSolicitud ticket, JwtAuthenticationToken auth) {
    String rol = getRol(auth);

    if (isBackoffice(rol)) {
      return;
    }

    if ("CLIENTE".equalsIgnoreCase(rol)) {
      UUID clienteId = UUID.fromString(auth.getToken().getSubject());
      if (ticket.getCliente() == null || !clienteId.equals(ticket.getCliente().getId())) {
        throw new SecurityException("No autorizado");
      }
      return;
    }

    throw new SecurityException("No autorizado");
  }

  private boolean isBackoffice(String rol) {
    return "ADMIN".equalsIgnoreCase(rol) || "TECNICO".equalsIgnoreCase(rol);
  }

  private String getRol(JwtAuthenticationToken auth) {
    Object rol = auth.getToken().getClaims().get("rol");
    return rol == null ? "" : String.valueOf(rol);
  }

  private String extractRelativePath(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String prefix = request.getContextPath() + "/files/";
    if (!uri.startsWith(prefix)) {
      return null;
    }
    return uri.substring(prefix.length());
  }

  private Resource toResource(Path path) {
    try {
      return new UrlResource(path.toUri());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Archivo no accesible");
    }
  }
}