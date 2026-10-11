package com.reparasuite.api.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import com.reparasuite.api.config.AppProperties;
import com.reparasuite.api.exception.WhatsappLinkGenerationException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;

@Service
public class WhatsappService {

  private static final String WHATSAPP_BASE_URL = "https://wa.me/";
  private static final String BRAND_NAME = "PAMBYTE";

  private static final String VENEZUELA_COUNTRY_CODE = "58";
  private static final int VENEZUELA_NATIONAL_NUMBER_LENGTH = 10;

  private static final String REGISTER_PATH = "/auth/register";
  private static final String PORTAL_PATH = "/portal";
  private static final String ORDER_TRACKING_PATH = "/portal/orden/";

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
   * Genera un enlace genérico de WhatsApp para invitar a un cliente
   * a registrarse en el Portal PAMBYTE.
   *
   * Actualmente utiliza el registro público.
   *
   * En una evolución posterior este flujo deberá sustituirse por una
   * invitación segura vinculada al cliente mediante un token temporal.
   */
  public String generarLinkInvitacionRegistro() {
    String registerUrl = construirFrontendUrl(REGISTER_PATH);
    String mensaje = construirMensajeInvitacionRegistro(registerUrl);

    return construirUrlSinTelefono(mensaje);
  }

  /**
   * Genera un enlace dirigido al teléfono registrado de un cliente
   * para recordarle el acceso a su Portal.
   */
  @Transactional(readOnly = true)
  public String generarLinkRecordatorioLogin(UUID clienteId) {
    Cliente cliente = clienteRepo.findById(clienteId)
        .orElseThrow(() -> new WhatsappLinkGenerationException(
            "No se encontró el cliente con id: " + clienteId
        ));

    String telefono = normalizarTelefonoParaWhatsapp(
        cliente.getTelefono()
    );

    String portalUrl = construirFrontendUrl(PORTAL_PATH);

    String mensaje = construirMensajeRecordatorioLogin(
        cliente.getNombre(),
        portalUrl
    );

    return construirUrlConTelefono(
        telefono,
        mensaje
    );
  }

  /**
   * Genera un enlace dirigido al cliente de una Orden de Trabajo
   * y lo lleva directamente al detalle de esa orden en el Portal.
   */
  @Transactional(readOnly = true)
  public String generarLinkSeguimientoOt(UUID otId) {
    OrdenTrabajo ot = ordenTrabajoRepo.findById(otId)
        .orElseThrow(() -> new WhatsappLinkGenerationException(
            "No se encontró la orden de trabajo con id: " + otId
        ));

    Cliente cliente = ot.getCliente();

    if (cliente == null) {
      throw new WhatsappLinkGenerationException(
          "La orden de trabajo no tiene un cliente asociado."
      );
    }

    String codigoOt = requireText(
        ot.getCodigo(),
        "La orden de trabajo no tiene un código válido."
    );

    String telefono = normalizarTelefonoParaWhatsapp(
        cliente.getTelefono()
    );

    String codigoOtPath = UriUtils.encodePathSegment(
        codigoOt,
        StandardCharsets.UTF_8
    );

    String seguimientoUrl = construirFrontendUrl(
        ORDER_TRACKING_PATH + codigoOtPath
    );

    String mensaje = construirMensajeSeguimientoOt(
        cliente.getNombre(),
        codigoOt,
        seguimientoUrl
    );

    return construirUrlConTelefono(
        telefono,
        mensaje
    );
  }

  /**
   * Construye el mensaje de invitación al registro.
   */
  private String construirMensajeInvitacionRegistro(
      String registerUrl
  ) {
    return String.join(
        "\n",
        "Hola.",
        "",
        "Te invitamos a utilizar *" + BRAND_NAME
            + "*, nuestro portal para el seguimiento de tus servicios técnicos.",
        "",
        "Desde el portal podrás consultar tus equipos y órdenes de trabajo.",
        "",
        "Regístrate aquí:",
        registerUrl
    );
  }

  /**
   * Construye el mensaje de recordatorio de acceso al Portal.
   */
  private String construirMensajeRecordatorioLogin(
      String nombreCliente,
      String portalUrl
  ) {
    return String.join(
        "\n",
        construirSaludo(nombreCliente),
        "",
        "Ya puedes consultar el estado actual de tus servicios en *"
            + BRAND_NAME + "*.",
        "",
        "Ingresa a tu portal aquí:",
        portalUrl
    );
  }

  /**
   * Construye el mensaje de seguimiento de una Orden de Trabajo.
   */
  private String construirMensajeSeguimientoOt(
      String nombreCliente,
      String codigoOt,
      String seguimientoUrl
  ) {
    return String.join(
        "\n",
        construirSaludo(nombreCliente),
        "",
        "Ya está disponible el seguimiento de tu Orden de Trabajo:",
        "*" + codigoOt + "*",
        "",
        "Consulta su estado directamente desde tu portal *"
            + BRAND_NAME + "*:",
        seguimientoUrl
    );
  }

  /**
   * Construye una URL absoluta del Portal a partir de una ruta interna.
   */
  private String construirFrontendUrl(String path) {
    String frontendUrl = obtenerFrontendUrlNormalizada();

    if (path == null || path.isBlank()) {
      return frontendUrl;
    }

    String normalizedPath = path.startsWith("/")
        ? path
        : "/" + path;

    return frontendUrl + normalizedPath;
  }

  /**
   * Obtiene y valida la URL base configurada para el frontend.
   */
  private String obtenerFrontendUrlNormalizada() {
    String frontendUrl = appProperties.frontendUrl();

    if (frontendUrl == null || frontendUrl.isBlank()) {
      throw new WhatsappLinkGenerationException(
          "La propiedad app.frontend-url no está configurada."
      );
    }

    String normalized = frontendUrl.trim();

    while (normalized.endsWith("/")) {
      normalized = normalized.substring(
          0,
          normalized.length() - 1
      );
    }

    try {
      URI uri = URI.create(normalized);

      String scheme = uri.getScheme();

      if (scheme == null
          || (!scheme.equalsIgnoreCase("http")
              && !scheme.equalsIgnoreCase("https"))) {

        throw new WhatsappLinkGenerationException(
            "La propiedad app.frontend-url debe utilizar HTTP o HTTPS."
        );
      }

      if (uri.getHost() == null || uri.getHost().isBlank()) {
        throw new WhatsappLinkGenerationException(
            "La propiedad app.frontend-url no contiene un host válido."
        );
      }

      if (uri.getUserInfo() != null) {
        throw new WhatsappLinkGenerationException(
            "La propiedad app.frontend-url no debe contener credenciales."
        );
      }

      if (uri.getQuery() != null) {
        throw new WhatsappLinkGenerationException(
            "La propiedad app.frontend-url no debe contener parámetros de consulta."
        );
      }

      if (uri.getFragment() != null) {
        throw new WhatsappLinkGenerationException(
            "La propiedad app.frontend-url no debe contener fragmentos."
        );
      }

    } catch (WhatsappLinkGenerationException ex) {
      throw ex;

    } catch (IllegalArgumentException ex) {
      throw new WhatsappLinkGenerationException(
          "La propiedad app.frontend-url contiene una URL inválida."
      );
    }

    return normalized;
  }

  /**
   * Construye un saludo consistente para los mensajes enviados
   * desde el sistema.
   */
  private String construirSaludo(String nombre) {
    String nombreSeguro = obtenerNombreSeguro(nombre);

    if (nombreSeguro == null) {
      return "Hola.";
    }

    return "Hola, *" + nombreSeguro + "*.";
  }

  /**
   * Genera un enlace wa.me sin destinatario específico.
   *
   * WhatsApp permitirá seleccionar el contacto antes de enviar.
   */
  private String construirUrlSinTelefono(String mensaje) {
    return WHATSAPP_BASE_URL
        + "?text="
        + codificarMensaje(mensaje);
  }

  /**
   * Genera un enlace wa.me dirigido a un teléfono determinado.
   */
  private String construirUrlConTelefono(
      String telefono,
      String mensaje
  ) {
    String telefonoSeguro = requireText(
        telefono,
        "El teléfono de WhatsApp no puede estar vacío."
    );

    return WHATSAPP_BASE_URL
        + telefonoSeguro
        + "?text="
        + codificarMensaje(mensaje);
  }

  /**
   * Codifica el texto como valor válido de un parámetro de consulta.
   */
  private String codificarMensaje(String mensaje) {
    String mensajeSeguro = requireText(
        mensaje,
        "El mensaje de WhatsApp no puede estar vacío."
    );

    return UriUtils.encodeQueryParam(
        mensajeSeguro,
        StandardCharsets.UTF_8
    );
  }

  /**
   * Normaliza teléfonos venezolanos al formato internacional
   * requerido por wa.me.
   *
   * Ejemplos válidos:
   *
   * 04241234567
   * 4241234567
   * +584241234567
   * 00584241234567
   * +58 424 123 4567
   *
   * Resultado:
   *
   * 584241234567
   *
   * Cuando ReparaSuite evolucione a multi-país, esta lógica deberá
   * sustituirse por normalización E.164 configurable por país.
   */
  private String normalizarTelefonoParaWhatsapp(String telefono) {
    if (telefono == null || telefono.isBlank()) {
      throw new WhatsappLinkGenerationException(
          "El cliente no tiene teléfono registrado."
      );
    }

    String digits = telefono.replaceAll("\\D", "");

    if (digits.startsWith("00")) {
      digits = digits.substring(2);
    }

    String nationalNumber;

    if (digits.startsWith(VENEZUELA_COUNTRY_CODE)) {
      nationalNumber = digits.substring(
          VENEZUELA_COUNTRY_CODE.length()
      );
    } else {
      nationalNumber = digits;
    }

    if (nationalNumber.startsWith("0")) {
      nationalNumber = nationalNumber.substring(1);
    }

    if (nationalNumber.length()
        != VENEZUELA_NATIONAL_NUMBER_LENGTH) {

      throw new WhatsappLinkGenerationException(
          "Formato de teléfono venezolano inválido para WhatsApp."
      );
    }

    return VENEZUELA_COUNTRY_CODE + nationalNumber;
  }

  /**
   * Devuelve un nombre apto para utilizar en una plantilla de WhatsApp.
   *
   * Se eliminan caracteres de formato que podrían alterar el mensaje.
   */
  private String obtenerNombreSeguro(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return null;
    }

    String normalized = nombre
        .replaceAll("\\p{Cntrl}", " ")
        .replace("*", "")
        .replace("_", "")
        .replace("~", "")
        .replace("`", "")
        .replaceAll("\\s+", " ")
        .trim();

    return normalized.isBlank()
        ? null
        : normalized;
  }

  /**
   * Validación reutilizable para valores textuales obligatorios.
   */
  private String requireText(
      String value,
      String errorMessage
  ) {
    if (value == null || value.isBlank()) {
      throw new WhatsappLinkGenerationException(
          errorMessage
      );
    }

    return value.trim();
  }
}