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
      NotaOtRepo notaRepo,
      FotoOtRepo fotoRepo,
      HistorialOtRepo historialRepo,
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
      // Si en tu proyecto añadiste email a Usuario, mantén esta firma (user, nombre, email, pass, rol)
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "admin", "Administrador", "admin@reparasuite.com", "admin123", RolUsuario.ADMIN);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec1", "Técnico Uno", "tec1@reparasuite.com", "tec123", RolUsuario.TECNICO);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec2", "Técnico Dos", "tec2@reparasuite.com", "tec123", RolUsuario.TECNICO);

      // Actor del historial para el seed (no hay JWT aquí)
      Usuario adminActor = usuarioRepo.findByUsuario("admin").orElseThrow();

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
        ot1 = otRepo.save(ot1);

        // ✅ Historial inicial OT_CREADA (enriquecido)
        crearHistorialCreacion(historialRepo, ot1, adminActor);

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
        ot2 = otRepo.save(ot2);

        // ✅ Historial inicial OT_CREADA (enriquecido)
        crearHistorialCreacion(historialRepo, ot2, adminActor);
      }
    };
  }

  // -------------------
  // Helpers
  // -------------------

  private void crearUsuarioSiNoExiste(UsuarioRepo repo, PasswordEncoder encoder,
                                     String user, String nombre, String email, String pass, RolUsuario rol) {
    if (repo.findByUsuario(user).isPresent()) return;

    Usuario u = new Usuario();
    u.setUsuario(user);
    u.setNombre(nombre);

    // Si tu entidad Usuario NO tiene email, elimina esta línea y ajusta la firma.
    u.setEmail(email);

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

  private void crearHistorialCreacion(HistorialOtRepo historialRepo, OrdenTrabajo ot, Usuario actor) {
    HistorialOt h = new HistorialOt();
    h.setOt(ot);
    h.setUsuario(actor);
    h.setEvento(EventoHistorialOt.OT_CREADA);

    String desc = "Orden creada (" + ot.getTipo().name() + "/" + ot.getPrioridad().name() + ") para " +
        ot.getCliente().getNombre() + ": " + recortar(ot.getDescripcion(), 200);

    h.setDescripcion(desc);

    // Para que el timeline arranque exactamente en la fecha de creación de la OT
    h.setFecha(ot.getCreatedAt());

    historialRepo.save(h);
  }

  private String recortar(String s, int max) {
    if (s == null) return "";
    String t = s.trim();
    if (t.length() <= max) return t;
    return t.substring(0, max - 1) + "…";
  }
}
