package com.reparasuite.api.config;

import java.math.BigDecimal;
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
      PresupuestoOtRepo presupuestoRepo,
      PagoOtRepo pagoRepo,
      HistorialOtRepo historialRepo,
      PasswordEncoder encoder
  ) {
    return args -> {

      // Taller fijo
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

      // Usuarios backoffice
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "admin", "Administrador", "admin@reparasuite.com", "admin123", RolUsuario.ADMIN);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec1", "Técnico Uno", "tec1@reparasuite.com", "tec123", RolUsuario.TECNICO);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec2", "Técnico Dos", "tec2@reparasuite.com", "tec123", RolUsuario.TECNICO);

      Usuario admin = usuarioRepo.findByUsuario("admin").orElseThrow();
      Usuario tec1 = usuarioRepo.findByUsuario("tec1").orElseThrow();

      // Clientes
      Cliente c1 = crearClienteSiNoExiste(clienteRepo, encoder, "Carlos Pérez", "611000111", "carlos@mail.com", "cliente123");
      Cliente c2 = crearClienteSiNoExiste(clienteRepo, encoder, "María López", "622000222", "maria@mail.com", "cliente123");
      Cliente c3 = crearClienteSiNoExiste(clienteRepo, encoder, "Gledys", "600123456", "gle@gmail.com", "123456");
      // OTs demo
      if (otRepo.count() == 0) {

        OrdenTrabajo ot1 = new OrdenTrabajo();
        ot1.setCodigo("OT-0001");
        ot1.setEstado(EstadoOt.RECIBIDA);
        ot1.setTipo(TipoOt.TIENDA);
        ot1.setPrioridad(PrioridadOt.MEDIA);
        ot1.setDescripcion("Portátil no enciende");
        ot1.setCliente(c1);
        ot1.setTecnico(tec1);
        ot1 = otRepo.save(ot1);
        crearHistorialCreacion(historialRepo, ot1, admin);

        OrdenTrabajo ot2 = new OrdenTrabajo();
        ot2.setCodigo("OT-0002");
        ot2.setEstado(EstadoOt.PRESUPUESTO);
        ot2.setTipo(TipoOt.DOMICILIO);
        ot2.setPrioridad(PrioridadOt.ALTA);
        ot2.setDescripcion("Instalación de router y revisión de cableado");
        ot2.setCliente(c2);
        ot2.setTecnico(tec1);
        ot2.setFechaPrevista(OffsetDateTime.now().plusDays(2));
        ot2.setDireccion("C/ Demo 123");
        ot2.setNotasAcceso("Llamar al llegar");
        ot2 = otRepo.save(ot2);
        crearHistorialCreacion(historialRepo, ot2, admin);

        // Presupuesto ENVIADO + pago asociado (para probar flujo cliente)
        PresupuestoOt p = new PresupuestoOt();
        p.setOt(ot2);
        p.setEstado(EstadoPresupuesto.ENVIADO);
        p.setImporte(new BigDecimal("120.00"));
        p.setDetalle("Instalación + revisión. Incluye desplazamiento.");
        p.setSentAt(OffsetDateTime.now().minusMinutes(20));
        p = presupuestoRepo.save(p);

        PagoOt pago = new PagoOt();
        pago.setOt(ot2);
        pago.setImporte(p.getImporte());
        pago.setEstado(EstadoPagoOt.PENDIENTE);
        pagoRepo.save(pago);

        HistorialOt h = new HistorialOt();
        h.setOt(ot2);
        h.setEvento(EventoHistorialOt.PRESUPUESTO_ENVIADO);
        h.setDescripcion("Presupuesto enviado al cliente");
        h.setActorTipo(ActorTipo.USUARIO);
        h.setActorNombre(admin.getNombre());
        h.setUsuario(admin);
        h.setFecha(p.getSentAt());
        historialRepo.save(h);
      }
    };
  }

  private void crearUsuarioSiNoExiste(UsuarioRepo repo, PasswordEncoder encoder,
                                     String user, String nombre, String email, String pass, RolUsuario rol) {
    if (repo.findByUsuario(user).isPresent()) return;

    Usuario u = new Usuario();
    u.setUsuario(user);
    u.setNombre(nombre);
    u.setEmail(email);
    u.setRol(rol);
    u.setActivo(true);
    u.setPasswordHash(encoder.encode(pass));
    repo.save(u);
  }

  private Cliente crearClienteSiNoExiste(ClienteRepo repo, PasswordEncoder encoder,
                                        String nombre, String tel, String email, String passPortal) {
    Cliente existing = repo.findAll().stream()
        .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
        .findFirst().orElse(null);

    if (existing != null) return existing;

    Cliente c = new Cliente();
    c.setNombre(nombre);
    c.setTelefono(tel);
    c.setEmail(email);

    // Portal activo
    c.setPortalActivo(true);
    c.setPasswordHashPortal(encoder.encode(passPortal));

    return repo.save(c);
  }

  private void crearHistorialCreacion(HistorialOtRepo historialRepo, OrdenTrabajo ot, Usuario actor) {
    HistorialOt h = new HistorialOt();
    h.setOt(ot);
    h.setUsuario(actor);
    h.setEvento(EventoHistorialOt.OT_CREADA);

    String desc = "Orden creada (" + ot.getTipo().name() + "/" + ot.getPrioridad().name() + ") para " +
        ot.getCliente().getNombre() + ": " + recortar(ot.getDescripcion(), 200);

    h.setDescripcion(desc);
    h.setActorTipo(ActorTipo.USUARIO);
    h.setActorNombre(actor.getNombre());
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
