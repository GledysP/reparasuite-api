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
import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PrioridadOt;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.model.TipoOt;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.TallerRepo;
import com.reparasuite.api.support.TestSecurityConfig;
import com.reparasuite.api.support.TestSecurityConfig.TestJwtFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OrdenTrabajoOwnershipIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestJwtFactory jwtFactory;

  @Autowired
  private ClienteRepo clienteRepo;

  @Autowired
  private OrdenTrabajoRepo otRepo;

  @Autowired
  private TallerRepo tallerRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Cliente clienteA;
  private Cliente clienteB;
  private OrdenTrabajo otA;

  @BeforeEach
  void setUp() {
    otRepo.deleteAll();
    clienteRepo.deleteAll();
    tallerRepo.deleteAll();

    Taller t = new Taller();
    t.setId(1L);
    t.setNombre("ReparaSuite Test");
    t.setPrefijoOt("OT");
    tallerRepo.save(t);

    clienteA = new Cliente();
    clienteA.setNombre("Cliente A");
    clienteA.setEmail("clientea-ot@test.com");
    clienteA.setTelefono("611111111");
    clienteA.setPortalActivo(true);
    clienteA.setPasswordHashPortal(passwordEncoder.encode("123456"));
    clienteA = clienteRepo.save(clienteA);

    clienteB = new Cliente();
    clienteB.setNombre("Cliente B");
    clienteB.setEmail("clienteb-ot@test.com");
    clienteB.setTelefono("622222222");
    clienteB.setPortalActivo(true);
    clienteB.setPasswordHashPortal(passwordEncoder.encode("123456"));
    clienteB = clienteRepo.save(clienteB);

    otA = new OrdenTrabajo();
    otA.setCodigo("OT-TEST-0001");
    otA.setCliente(clienteA);
    otA.setEstado(EstadoOt.RECIBIDA);
    otA.setTipo(TipoOt.TIENDA);
    otA.setPrioridad(PrioridadOt.MEDIA);
    otA.setDescripcion("Equipo no enciende");
    otA.setEquipo("Portátil Lenovo");
    otA = otRepo.save(otA);
  }

  @Test
  void clientePropietarioDebePoderVerSuOt() throws Exception {
    String token = jwtFactory.clienteToken(clienteA.getId(), clienteA.getEmail(), clienteA.getNombre());

    mockMvc.perform(get("/api/v1/ordenes-trabajo/" + otA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void otroClienteNoDebePoderVerOtAjena() throws Exception {
    String token = jwtFactory.clienteToken(clienteB.getId(), clienteB.getEmail(), clienteB.getNombre());

    mockMvc.perform(get("/api/v1/ordenes-trabajo/" + otA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void backofficeDebePoderVerOt() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "admin",
        "Administrador",
        "ADMIN"
    );

    mockMvc.perform(get("/api/v1/ordenes-trabajo/" + otA.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}