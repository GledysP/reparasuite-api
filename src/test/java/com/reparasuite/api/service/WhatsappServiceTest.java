package com.reparasuite.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriUtils;

import com.reparasuite.api.config.AppProperties;
import com.reparasuite.api.exception.WhatsappLinkGenerationException;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;

@ExtendWith(MockitoExtension.class)
class WhatsappServiceTest {

  private static final String FRONTEND_URL =
      "http://localhost:4300";

  @Mock
  private AppProperties appProperties;

  @Mock
  private ClienteRepo clienteRepo;

  @Mock
  private OrdenTrabajoRepo ordenTrabajoRepo;

  @Mock
  private Cliente cliente;

  @Mock
  private OrdenTrabajo ordenTrabajo;

  private WhatsappService whatsappService;

  @BeforeEach
  void setUp() {
    whatsappService = new WhatsappService(
        appProperties,
        clienteRepo,
        ordenTrabajoRepo
    );
  }

  @Test
  void debeGenerarInvitacionDeRegistroSinTelefono() {
    configurarFrontendLocal();

    String url =
        whatsappService.generarLinkInvitacionRegistro();

    assertTrue(
        url.startsWith("https://wa.me/?text=")
    );

    String mensaje = extraerMensaje(url);

    assertTrue(
        mensaje.contains("PAMBYTE")
    );

    assertTrue(
        mensaje.contains(
            FRONTEND_URL + "/auth/register"
        )
    );
  }

  @Test
  void debeGenerarRecordatorioLoginConTelefonoVenezolanoNormalizado() {
    configurarFrontendLocal();

    UUID clienteId = UUID.randomUUID();

    when(clienteRepo.findById(clienteId))
        .thenReturn(Optional.of(cliente));

    when(cliente.getTelefono())
        .thenReturn("0424-123-4567");

    when(cliente.getNombre())
        .thenReturn("María Pérez");

    String url =
        whatsappService.generarLinkRecordatorioLogin(
            clienteId
        );

    assertTrue(
        url.startsWith(
            "https://wa.me/584241234567?text="
        )
    );

    String mensaje = extraerMensaje(url);

    assertTrue(
        mensaje.contains(
            "Hola, *María Pérez*."
        )
    );

    assertTrue(
        mensaje.contains(
            FRONTEND_URL + "/portal"
        )
    );
  }

  @Test
  void debeGenerarSeguimientoDirectoDeOrdenTrabajo() {
    configurarFrontendLocal();

    UUID otId = UUID.randomUUID();

    when(ordenTrabajoRepo.findById(otId))
        .thenReturn(Optional.of(ordenTrabajo));

    when(ordenTrabajo.getCliente())
        .thenReturn(cliente);

    when(ordenTrabajo.getCodigo())
        .thenReturn("OT-20260913-0001");

    when(cliente.getTelefono())
        .thenReturn("+58 424 123 4567");

    when(cliente.getNombre())
        .thenReturn("Gledys Palma");

    String url =
        whatsappService.generarLinkSeguimientoOt(
            otId
        );

    assertTrue(
        url.startsWith(
            "https://wa.me/584241234567?text="
        )
    );

    String mensaje = extraerMensaje(url);

    assertTrue(
        mensaje.contains(
            "*OT-20260913-0001*"
        )
    );

    assertTrue(
        mensaje.contains(
            FRONTEND_URL
                + "/portal/orden/OT-20260913-0001"
        )
    );
  }

  @Test
  void debeEliminarCaracteresDeFormatoDelNombre() {
    configurarFrontendLocal();

    UUID clienteId = UUID.randomUUID();

    when(clienteRepo.findById(clienteId))
        .thenReturn(Optional.of(cliente));

    when(cliente.getTelefono())
        .thenReturn("4241234567");

    when(cliente.getNombre())
        .thenReturn("*María* _Pérez_");

    String url =
        whatsappService.generarLinkRecordatorioLogin(
            clienteId
        );

    String mensaje = extraerMensaje(url);

    assertTrue(
        mensaje.contains(
            "Hola, *María Pérez*."
        )
    );
  }

  @Test
  void debeFallarSiClienteNoExiste() {
    UUID clienteId = UUID.randomUUID();

    when(clienteRepo.findById(clienteId))
        .thenReturn(Optional.empty());

    WhatsappLinkGenerationException exception =
        assertThrows(
            WhatsappLinkGenerationException.class,
            () ->
                whatsappService.generarLinkRecordatorioLogin(
                    clienteId
                )
        );

    assertTrue(
        exception.getMessage().contains(
            "No se encontró el cliente"
        )
    );
  }

  @Test
  void debeFallarSiOrdenNoTieneCliente() {
    UUID otId = UUID.randomUUID();

    when(ordenTrabajoRepo.findById(otId))
        .thenReturn(Optional.of(ordenTrabajo));

    when(ordenTrabajo.getCliente())
        .thenReturn(null);

    WhatsappLinkGenerationException exception =
        assertThrows(
            WhatsappLinkGenerationException.class,
            () ->
                whatsappService.generarLinkSeguimientoOt(
                    otId
                )
        );

    assertEquals(
        "La orden de trabajo no tiene un cliente asociado.",
        exception.getMessage()
    );
  }

  @Test
  void debeFallarSiTelefonoVenezolanoEsInvalido() {
    UUID clienteId = UUID.randomUUID();

    when(clienteRepo.findById(clienteId))
        .thenReturn(Optional.of(cliente));

    when(cliente.getTelefono())
        .thenReturn("12345");

    WhatsappLinkGenerationException exception =
        assertThrows(
            WhatsappLinkGenerationException.class,
            () ->
                whatsappService.generarLinkRecordatorioLogin(
                    clienteId
                )
        );

    assertEquals(
        "Formato de teléfono venezolano inválido para WhatsApp.",
        exception.getMessage()
    );
  }

  @Test
  void debeFallarSiFrontendUrlNoEsHttpNiHttps() {
    when(appProperties.frontendUrl())
        .thenReturn("ftp://localhost:4300");

    WhatsappLinkGenerationException exception =
        assertThrows(
            WhatsappLinkGenerationException.class,
            () ->
                whatsappService.generarLinkInvitacionRegistro()
        );

    assertEquals(
        "La propiedad app.frontend-url debe utilizar HTTP o HTTPS.",
        exception.getMessage()
    );
  }

  private void configurarFrontendLocal() {
    when(appProperties.frontendUrl())
        .thenReturn(FRONTEND_URL);
  }

  private String extraerMensaje(
      String whatsappUrl
  ) {
    URI uri = URI.create(whatsappUrl);

    String rawQuery = uri.getRawQuery();

    assertTrue(
        rawQuery != null
            && rawQuery.startsWith("text=")
    );

    String encodedMessage =
        rawQuery.substring(
            "text=".length()
        );

    return UriUtils.decode(
        encodedMessage,
        StandardCharsets.UTF_8
    );
  }
}