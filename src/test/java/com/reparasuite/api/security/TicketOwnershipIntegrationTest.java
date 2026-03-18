package com.reparasuite.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoTicket;
import com.reparasuite.api.model.TicketSolicitud;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TicketSolicitudRepo;
import com.reparasuite.api.support.TestSecurityConfig;
import com.reparasuite.api.support.TestSecurityConfig.TestJwtFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TicketOwnershipIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestJwtFactory jwtFactory;

  @Autowired
  private ClienteRepo clienteRepo;

  @Autowired
  private TicketSolicitudRepo ticketRepo;

  @Autowired
  private OrdenTrabajoRepo ordenTrabajoRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Cliente clienteA;
  private Cliente clienteB;
  private TicketSolicitud ticketA;

  @BeforeEach
  void setUp() {
    ticketRepo.deleteAll();
    ordenTrabajoRepo.deleteAll();
    clienteRepo.deleteAll();

    clienteA = new Cliente();
    clienteA.setNombre("Cliente A");
    clienteA.setEmail("clientea@test.com");
    clienteA.setTelefono("600000001");
    clienteA.setPortalActivo(true);
    clienteA.setPasswordHashPortal(passwordEncoder.encode("123456"));
    clienteA = clienteRepo.save(clienteA);

    clienteB = new Cliente();
    clienteB.setNombre("Cliente B");
    clienteB.setEmail("clienteb@test.com");
    clienteB.setTelefono("600000002");
    clienteB.setPortalActivo(true);
    clienteB.setPasswordHashPortal(passwordEncoder.encode("123456"));
    clienteB = clienteRepo.save(clienteB);

    ticketA = new TicketSolicitud();
    ticketA.setCliente(clienteA);
    ticketA.setEstado(EstadoTicket.ABIERTO);
    ticketA.setAsunto("Router caído");
    ticketA.setDescripcion("No hay internet");
    ticketA.setClienteNombreSnapshot(clienteA.getNombre());
    ticketA.setClienteTelefonoSnapshot(clienteA.getTelefono());
    ticketA.setClienteEmailSnapshot(clienteA.getEmail());
    ticketA = ticketRepo.save(ticketA);
  }

  @Test
  void clientePropietarioDebePoderVerSuTicket() throws Exception {
    String token = jwtFactory.clienteToken(clienteA.getId(), clienteA.getEmail(), clienteA.getNombre());

    mockMvc.perform(get("/api/v1/tickets/" + ticketA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void otroClienteNoDebePoderVerTicketAjeno() throws Exception {
    String token = jwtFactory.clienteToken(clienteB.getId(), clienteB.getEmail(), clienteB.getNombre());

    mockMvc.perform(get("/api/v1/tickets/" + ticketA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void backofficeDebePoderVerTicketEnEndpointBackoffice() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "admin",
        "Administrador",
        "ADMIN"
    );

    mockMvc.perform(get("/api/v1/backoffice/tickets/" + ticketA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}