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

  public String generarLinkInvitacionRegistro() {
    String frontendUrl = obtenerFrontendUrlNormalizada();
    String registerUrl = frontendUrl + "/auth/register";

    String mensaje = "¡Hola! 👋 Te invito a registrarte en *ReparaSuite* para gestionar tus equipos "
        + "y ver tus órdenes en tiempo real. Regístrate aquí: " + registerUrl;

    return construirUrlSinTelefono(mensaje);
  }

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
      mensaje = "¡Hola! 👋 Puedes ver el estado actual de tu equipo en *ReparaSuite* aquí: "
          + dashboardUrl;
    } else {
      mensaje = "¡Hola! 👋 *" + nombreCliente + "*, puedes ver el estado actual de tu equipo "
          + "en *ReparaSuite* aquí: " + dashboardUrl;
    }

    return construirUrlConTelefono(telefono, mensaje);
  }

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
      mensaje = "¡Hola! 👋 Puedes ver el estado actual de tu orden en *ReparaSuite* aquí: "
          + dashboardUrl;
    } else {
      mensaje = "¡Hola! 👋 *" + nombreCliente + "*, puedes ver el estado actual de tu orden "
          + "en *ReparaSuite* aquí: " + dashboardUrl;
    }

    return construirUrlConTelefono(telefono, mensaje);
  }

  private String obtenerFrontendUrlNormalizada() {
    String frontendUrl = appProperties.frontendUrl();

    if (frontendUrl == null || frontendUrl.isBlank()) {
      throw new WhatsappLinkGenerationException(
          "La propiedad app.frontend-url no está configurada."
      );
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

  private String normalizarTelefonoParaWhatsapp(String telefono) {
    if (telefono == null || telefono.isBlank()) {
      throw new WhatsappLinkGenerationException(
          "El cliente no tiene teléfono registrado."
      );
    }

    String normalizado = telefono.replaceAll("\\D", "");

    if (normalizado.isBlank()) {
      throw new WhatsappLinkGenerationException(
          "El teléfono del cliente no contiene dígitos válidos."
      );
    }

    return normalizado;
  }

  private String obtenerNombreSeguro(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return null;
    }
    return nombre.trim();
  }
}