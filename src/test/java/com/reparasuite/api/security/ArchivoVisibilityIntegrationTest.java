package com.reparasuite.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.FotoOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PrioridadOt;
import com.reparasuite.api.model.TipoOt;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.FotoOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.support.TestSecurityConfig;
import com.reparasuite.api.support.TestSecurityConfig.TestJwtFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ArchivoVisibilityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestJwtFactory jwtFactory;

  @Autowired
  private ClienteRepo clienteRepo;

  @Autowired
  private OrdenTrabajoRepo otRepo;

  @Autowired
  private FotoOtRepo fotoOtRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  private Cliente cliente;
  private OrdenTrabajo ot;
  private String visibleFilename;
  private String internoFilename;

  @BeforeEach
  void setUp() throws Exception {
    fotoOtRepo.deleteAll();
    otRepo.deleteAll();
    clienteRepo.deleteAll();

    cliente = new Cliente();
    cliente.setNombre("Cliente Archivos");
    cliente.setEmail("cliente-archivos@test.com");
    cliente.setTelefono("633333333");
    cliente.setPortalActivo(true);
    cliente.setPasswordHashPortal(passwordEncoder.encode("123456"));
    cliente = clienteRepo.save(cliente);

    ot = new OrdenTrabajo();
    ot.setCodigo("OT-FILE-0001");
    ot.setCliente(cliente);
    ot.setEstado(EstadoOt.RECIBIDA);
    ot.setTipo(TipoOt.TIENDA);
    ot.setPrioridad(PrioridadOt.MEDIA);
    ot.setDescripcion("OT con archivos");
    ot.setEquipo("TV Samsung");
    ot = otRepo.save(ot);

    visibleFilename = UUID.randomUUID() + ".png";
    internoFilename = UUID.randomUUID() + ".png";

    FotoOt visible = new FotoOt();
    visible.setOt(ot);
    visible.setUrl("/api/v1/archivos/ot/" + ot.getId() + "/" + visibleFilename);
    visible.setVisibleCliente(true);
    fotoOtRepo.save(visible);

    FotoOt interno = new FotoOt();
    interno.setOt(ot);
    interno.setUrl("/api/v1/archivos/ot/" + ot.getId() + "/" + internoFilename);
    interno.setVisibleCliente(false);
    fotoOtRepo.save(interno);

    Path dir = Paths.get(uploadDir).resolve("ot").resolve(ot.getId().toString());
    Files.createDirectories(dir);

    byte[] pngBytes = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D
    };

    Files.write(dir.resolve(visibleFilename), pngBytes);
    Files.write(dir.resolve(internoFilename), pngBytes);
  }

  @Test
  void clienteDebePoderDescargarFotoVisible() throws Exception {
    String token = jwtFactory.clienteToken(cliente.getId(), cliente.getEmail(), cliente.getNombre());

    mockMvc.perform(get("/api/v1/archivos/ot/" + ot.getId() + "/" + visibleFilename)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void clienteNoDebePoderDescargarFotoInterna() throws Exception {
    String token = jwtFactory.clienteToken(cliente.getId(), cliente.getEmail(), cliente.getNombre());

    mockMvc.perform(get("/api/v1/archivos/ot/" + ot.getId() + "/" + internoFilename)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void backofficeDebePoderDescargarFotoInterna() throws Exception {
    String token = jwtFactory.backofficeToken(
        UUID.randomUUID(),
        "admin",
        "Administrador",
        "ADMIN"
    );

    mockMvc.perform(get("/api/v1/archivos/ot/" + ot.getId() + "/" + internoFilename)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}