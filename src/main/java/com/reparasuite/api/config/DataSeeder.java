package com.reparasuite.api.config;

import java.time.OffsetDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reparasuite.api.model.*;
import com.reparasuite.api.repo.*;

@Configuration
public class DataSeeder {

  @Bean
  CommandLineRunner seed(
      TallerRepo tallerRepo,
      UsuarioRepo usuarioRepo,
      ClienteRepo clienteRepo,
      OrdenTrabajoRepo otRepo,
      PasswordEncoder encoder
  ) {
    return args -> {

      // 1) Taller fijo
      if (tallerRepo.findById(1L).isEmpty()) {
        Taller t = new Taller();
        t.setId(1L);
        t.setNombre("Repara Suite Demo");
        t.setTelefono("600111222");
        t.setEmail("demo@reparasuite.com");
        t.setDireccion("Murcia (demo)");
        t.setPrefijoOt("OT");
        tallerRepo.save(t);
      }

      // 2) Usuarios
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "admin", "Administrador", "admin123", RolUsuario.ADMIN);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec1", "Técnico Uno", "tec123", RolUsuario.TECNICO);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec2", "Técnico Dos", "tec123", RolUsuario.TECNICO);

      // 3) Clientes
      Cliente c1 = crearClienteSiNoExiste(clienteRepo, "Carlos Pérez", "611000111", "carlos@mail.com");
      Cliente c2 = crearClienteSiNoExiste(clienteRepo, "María López", "622000222", "maria@mail.com");

      // 4) OTs demo (si no hay)
      if (otRepo.count() == 0) {
        Usuario tec1 = usuarioRepo.findByUsuario("tec1").orElseThrow();

        OrdenTrabajo ot1 = new OrdenTrabajo();
        ot1.setCodigo("OT-0001");
        ot1.setEstado(EstadoOt.RECIBIDA);
        ot1.setTipo(TipoOt.TIENDA);
        ot1.setPrioridad(PrioridadOt.MEDIA);
        ot1.setDescripcion("Portátil no enciende");
        ot1.setCliente(c1);
        ot1.setTecnico(tec1);
        otRepo.save(ot1);

        OrdenTrabajo ot2 = new OrdenTrabajo();
        ot2.setCodigo("OT-0002");
        ot2.setEstado(EstadoOt.EN_CURSO);
        ot2.setTipo(TipoOt.DOMICILIO);
        ot2.setPrioridad(PrioridadOt.ALTA);
        ot2.setDescripcion("Instalación de router y revisión de cableado");
        ot2.setCliente(c2);
        ot2.setTecnico(tec1);
        ot2.setFechaPrevista(OffsetDateTime.now().plusDays(2));
        ot2.setDireccion("C/ Demo 123");
        ot2.setNotasAcceso("Llamar al llegar");
        otRepo.save(ot2);
      }
    };
  }

  private void crearUsuarioSiNoExiste(UsuarioRepo repo, PasswordEncoder encoder,
                                     String user, String nombre, String pass, RolUsuario rol) {
    if (repo.findByUsuario(user).isPresent()) return;

    Usuario u = new Usuario();
    u.setUsuario(user);
    u.setNombre(nombre);
    u.setRol(rol);
    u.setActivo(true);
    u.setPasswordHash(encoder.encode(pass));
    repo.save(u);
  }

  private Cliente crearClienteSiNoExiste(ClienteRepo repo, String nombre, String tel, String email) {
    // MVP simple: si existe uno con mismo nombre, lo reutilizamos
    return repo.findAll().stream()
        .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
        .findFirst()
        .orElseGet(() -> {
          Cliente c = new Cliente();
          c.setNombre(nombre);
          c.setTelefono(tel);
          c.setEmail(email);
          return repo.save(c);
        });
  }
}
