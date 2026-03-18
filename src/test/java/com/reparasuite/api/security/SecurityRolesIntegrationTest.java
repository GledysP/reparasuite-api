package com.reparasuite.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.reparasuite.api.support.TestSecurityConfig;
import com.reparasuite.api.support.TestSecurityConfig.TestJwtFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SecurityRolesIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestJwtFactory jwtFactory;

  @Test
  void adminDebePoderAccederAUsuarios() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "admin",
        "Administrador",
        "ADMIN"
    );

    mockMvc.perform(get("/api/v1/usuarios")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void tecnicoNoDebePoderAccederAUsuariosSoloAdmin() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "tec1",
        "Tecnico Uno",
        "TECNICO"
    );

    mockMvc.perform(get("/api/v1/usuarios")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void clienteNoDebePoderAccederAClientesBackoffice() throws Exception {
    String token = jwtFactory.clienteToken(
        UUID.randomUUID(),
        "cliente@test.com",
        "Cliente Test"
    );

    mockMvc.perform(get("/api/v1/clientes")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void tecnicoDebePoderAccederAClientes() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "tec1",
        "Tecnico Uno",
        "TECNICO"
    );

    mockMvc.perform(get("/api/v1/clientes")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void sinTokenDebeResponder401() throws Exception {
    mockMvc.perform(get("/api/v1/clientes"))
        .andExpect(status().isUnauthorized());
  }
}