package com.reparasuite.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.reparasuite.api.config.AppProperties;
import com.reparasuite.api.exception.WhatsappLinkGenerationException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;

@Service
public class WhatsappService {

  private static final String WHATSAPP_BASE_URL = "https://wa.me/";

  private final AppProperties appProperties;
  private final ClienteRepo clienteRepo;
  private final OrdenTrabajoRepo ordenTrabajoRepo;

  public WhatsappService(
      AppProperties appProperties,
      ClienteRepo clienteRepo,
      OrdenTrabajoRepo ordenTrabajoRepo
  ) {
    this.appProperties = appProperties;
    this.clienteRepo = clienteRepo;
    this.ordenTrabajoRepo = ordenTrabajoRepo;
  }

  /**
   * Genera link para invitar a un nuevo cliente al ecosistema PAMBYTE.
   */
  public String generarLinkInvitacionRegistro() {
    String frontendUrl = obtenerFrontendUrlNormalizada();
    String registerUrl = frontendUrl + "/auth/register";

    String mensaje = "✨ ¡Hola! 👋 Te invitamos a unirte a *PAMBYTE Enterprise*, el portal oficial para "
        + "el seguimiento de tus reparaciones en *ReparaSuite*.\n\n"
        + "📲 *Regístrate ahora* para ver el estado de tus equipos en tiempo real:\n"
        + "🔗 " + registerUrl;

    return construirUrlSinTelefono(mensaje);
  }

  /**
   * Genera link para que el cliente ingrese a su panel de control.
   */
  public String generarLinkRecordatorioLogin(UUID clienteId) {
    Cliente cliente = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new WhatsappLinkGenerationException(
            "No se encontró el cliente con id: " + clienteId
        ));

    String telefono = normalizarTelefonoParaWhatsapp(cliente.getTelefono());
    String frontendUrl = obtenerFrontendUrlNormalizada();
    String dashboardUrl = frontendUrl + "/app";
    String nombreCliente = obtenerNombreSeguro(cliente.getNombre());

    String mensaje;
    if (nombreCliente == null) {
      mensaje = "✅ ¡Hola! 👋 Ya puedes ver el estado actual de tu equipo en *PAMBYTE*\n"
          + "👉 Ingresa a tu portal aquí: " + dashboardUrl;
    } else {
      mensaje = "✅ ¡Hola! 👋 *_" + nombreCliente + "_*, te recordamos que puedes ver el "
          + "estado actual de tu equipo en *PAMBYTE*\n"
          + "👉 Ingresa a tu portal aquí: " + dashboardUrl;
    }

    return construirUrlConTelefono(telefono, mensaje);
  }

  /**
   * Genera link de seguimiento para una Orden de Trabajo específica.
   */
  public String generarLinkSeguimientoOt(UUID otId) {
    OrdenTrabajo ot = ordenTrabajoRepo.findById(otId)
        .orElseThrow(() -> new WhatsappLinkGenerationException(
            "No se encontró la orden de trabajo con id: " + otId
        ));

    Cliente cliente = ot.getCliente();
    if (cliente == null) {
      throw new WhatsappLinkGenerationException("La orden de trabajo no tiene cliente asociado.");
    }

    String telefono = normalizarTelefonoParaWhatsapp(cliente.getTelefono());
    String frontendUrl = obtenerFrontendUrlNormalizada();
    String dashboardUrl = frontendUrl + "/app";
    String nombreCliente = obtenerNombreSeguro(cliente.getNombre());

    String mensaje;
    if (nombreCliente == null) {
      mensaje = "🔎 ¡Hola! 👋 Ya está disponible el seguimiento de tu Orden: *" + ot.getCodigo() + "*\n\n"
          + "Míralo en tiempo real en tu portal *PAMBYTE*:\n"
          + "🔗 " + dashboardUrl;
    } else {
      mensaje = "🔎 ¡Hola! 👋 *_" + nombreCliente + "_*, ya está disponible el seguimiento de tu Orden: *" + ot.getCodigo() + "*\n\n"
          + "Míralo en tiempo real en tu portal *PAMBYTE*:\n"
          + "🔗 " + dashboardUrl;
    }

    return construirUrlConTelefono(telefono, mensaje);
  }

  private String obtenerFrontendUrlNormalizada() {
    String frontendUrl = appProperties.frontendUrl();
    if (frontendUrl == null || frontendUrl.isBlank()) {
      throw new WhatsappLinkGenerationException("La propiedad app.frontend-url no está configurada.");
    }
    String valor = frontendUrl.trim();
    return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
  }

  private String construirUrlSinTelefono(String mensaje) {
    return WHATSAPP_BASE_URL + "?text=" + codificar(mensaje);
  }

  private String construirUrlConTelefono(String telefono, String mensaje) {
    return WHATSAPP_BASE_URL + telefono + "?text=" + codificar(mensaje);
  }

  private String codificar(String valor) {
    return URLEncoder.encode(valor, StandardCharsets.UTF_8);
  }

  /**
   * Normaliza el teléfono para el formato internacional wa.me (58 + número sin 0).
   */
  private String normalizarTelefonoParaWhatsapp(String telefono) {
    if (telefono == null || telefono.isBlank()) {
      throw new WhatsappLinkGenerationException("El cliente no tiene teléfono registrado.");
    }

    // 1. Quitar todo lo que no sea número
    String normalizado = telefono.replaceAll("\\D", "");

    // 2. Quitar el '0' inicial (ej: 0424 -> 424)
    if (normalizado.startsWith("0")) {
      normalizado = normalizado.substring(1);
    }

    // 3. Agregar el prefijo de Venezuela 58 si no lo tiene
    if (!normalizado.startsWith("58")) {
      normalizado = "58" + normalizado;
    }

    if (normalizado.length() < 10) {
      throw new WhatsappLinkGenerationException("Formato de teléfono inválido para WhatsApp: " + telefono);
    }

    return normalizado;
  }

  private String obtenerNombreSeguro(String nombre) {
    if (nombre == null || nombre.isBlank()) return null;
    return nombre.trim();
  }
}