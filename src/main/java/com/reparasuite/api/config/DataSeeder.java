package com.reparasuite.api.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.CategoriaEquipo;
import com.reparasuite.api.model.CategoriaEquipoFalla;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.Equipo;
import com.reparasuite.api.model.EstadoOt;
import com.reparasuite.api.model.EstadoPagoOt;
import com.reparasuite.api.model.EstadoPresupuesto;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.PagoOt;
import com.reparasuite.api.model.PresupuestoOt;
import com.reparasuite.api.model.PrioridadOt;
import com.reparasuite.api.model.RolUsuario;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.model.TipoOt;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.CategoriaEquipoFallaRepo;
import com.reparasuite.api.repo.CategoriaEquipoRepo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.EquipoRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.PagoOtRepo;
import com.reparasuite.api.repo.PresupuestoOtRepo;
import com.reparasuite.api.repo.TallerRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Configuration
@Profile("dev")
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
      CategoriaEquipoRepo categoriaEquipoRepo,
      CategoriaEquipoFallaRepo categoriaEquipoFallaRepo,
      EquipoRepo equipoRepo,
      PasswordEncoder encoder
  ) {
    return args -> {

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

      crearUsuarioSiNoExiste(usuarioRepo, encoder, "admin", "Administrador", "admin@reparasuite.com", "admin123", RolUsuario.ADMIN);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec1", "Técnico Uno", "tec1@reparasuite.com", "tec123", RolUsuario.TECNICO);
      crearUsuarioSiNoExiste(usuarioRepo, encoder, "tec2", "Técnico Dos", "tec2@reparasuite.com", "tec123", RolUsuario.TECNICO);

      Usuario admin = usuarioRepo.findByUsuario("admin").orElseThrow();
      Usuario tec1 = usuarioRepo.findByUsuario("tec1").orElseThrow();

      Cliente c1 = crearClienteSiNoExiste(clienteRepo, encoder, "Carlos Pérez", "611000111", "carlos@mail.com", "cliente123");
      Cliente c2 = crearClienteSiNoExiste(clienteRepo, encoder, "María López", "622000222", "maria@mail.com", "cliente123");
      Cliente c3 = crearClienteSiNoExiste(clienteRepo, encoder, "Gledys", "600123456", "gle@gmail.com", "123456");

      CategoriaEquipo tv = crearCategoriaSiNoExiste(
          categoriaEquipoRepo,
          "TV",
          "Televisores",
          "TV, Smart TV, paneles y monitores domésticos",
          "tv",
          10
      );

      CategoriaEquipo portatil = crearCategoriaSiNoExiste(
          categoriaEquipoRepo,
          "PORTATIL",
          "Portátiles",
          "Ordenadores portátiles de cliente",
          "laptop_mac",
          20
      );

      CategoriaEquipo router = crearCategoriaSiNoExiste(
          categoriaEquipoRepo,
          "ROUTER",
          "Routers y red",
          "Routers, extensores, ONT y conectividad",
          "router",
          30
      );

      crearFallaSiNoExiste(categoriaEquipoFallaRepo, tv, "NO_ENCIENDE", "No enciende", "Equipo no da señal de encendido", 10);
      crearFallaSiNoExiste(categoriaEquipoFallaRepo, tv, "SIN_IMAGEN", "Sin imagen", "Enciende pero no muestra imagen", 20);
      crearFallaSiNoExiste(categoriaEquipoFallaRepo, tv, "PANTALLA_DANADA", "Pantalla dañada", "Golpe, líneas o panel averiado", 30);

      crearFallaSiNoExiste(categoriaEquipoFallaRepo, portatil, "NO_ENCIENDE", "No enciende", "Portátil no inicia o no responde", 10);
      crearFallaSiNoExiste(categoriaEquipoFallaRepo, portatil, "BATERIA", "Batería", "Descarga rápida o no carga", 20);
      crearFallaSiNoExiste(categoriaEquipoFallaRepo, portatil, "PANTALLA", "Pantalla", "Problemas de pantalla o bisagras", 30);

      crearFallaSiNoExiste(categoriaEquipoFallaRepo, router, "SIN_INTERNET", "Sin internet", "Cliente indica caída de servicio", 10);
      crearFallaSiNoExiste(categoriaEquipoFallaRepo, router, "CONFIGURACION", "Configuración", "Ajuste de red o puesta en marcha", 20);

      crearEquipoSiNoExiste(
          equipoRepo,
          c1,
          portatil,
          "Laptop Lenovo",
          "Lenovo",
          "ThinkPad E14",
          "SN-LEN-0001",
          "Despacho",
          "Equipo corporativo de uso diario"
      );

      crearEquipoSiNoExiste(
          equipoRepo,
          c2,
          tv,
          "Televisor Samsung",
          "Samsung",
          "Crystal UHD 55",
          "SN-TV-0002",
          "Salón",
          "TV principal del domicilio"
      );

      crearEquipoSiNoExiste(
          equipoRepo,
          c3,
          router,
          "Router TP-Link",
          "TP-Link",
          "Archer AX55",
          "SN-ROUT-0003",
          "Entrada",
          "Router principal"
      );

      if (otRepo.count() == 0) {

        OrdenTrabajo ot1 = new OrdenTrabajo();
        ot1.setCodigo("OT-0001");
        ot1.setEstado(EstadoOt.RECIBIDA);
        ot1.setTipo(TipoOt.TIENDA);
        ot1.setPrioridad(PrioridadOt.MEDIA);
        ot1.setEquipo("Laptop Lenovo");
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
        ot2.setEquipo("Router TP-Link");
        ot2.setDescripcion("Instalación de router y revisión de cableado");
        ot2.setCliente(c2);
        ot2.setTecnico(tec1);
        ot2.setFechaPrevista(OffsetDateTime.now().plusDays(2));
        ot2.setDireccion("C/ Demo 123");
        ot2.setNotasAcceso("Llamar al llegar");
        ot2 = otRepo.save(ot2);
        crearHistorialCreacion(historialRepo, ot2, admin);

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

  private void crearUsuarioSiNoExiste(
      UsuarioRepo repo,
      PasswordEncoder encoder,
      String user,
      String nombre,
      String email,
      String pass,
      RolUsuario rol
  ) {
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

  private Cliente crearClienteSiNoExiste(
      ClienteRepo repo,
      PasswordEncoder encoder,
      String nombre,
      String tel,
      String email,
      String passPortal
  ) {
    String emailNorm = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();

    if (emailNorm != null) {
      var byEmail = repo.findByEmailIgnoreCase(emailNorm);
      if (byEmail.isPresent()) {
        Cliente c = byEmail.get();

        if ((c.getNombre() == null || c.getNombre().isBlank()) && nombre != null && !nombre.isBlank()) {
          c.setNombre(nombre);
        }
        if ((c.getTelefono() == null || c.getTelefono().isBlank()) && tel != null && !tel.isBlank()) {
          c.setTelefono(tel);
        }
        if (Boolean.FALSE.equals(c.isPortalActivo())) {
          c.setPortalActivo(true);
        }
        if (c.getPasswordHashPortal() == null || c.getPasswordHashPortal().isBlank()) {
          c.setPasswordHashPortal(encoder.encode(passPortal));
        }

        return repo.save(c);
      }
    }

    Cliente existingByNombre = repo.findAll().stream()
        .filter(c -> c.getNombre() != null && nombre != null && c.getNombre().equalsIgnoreCase(nombre))
        .findFirst()
        .orElse(null);

    if (existingByNombre != null) {
      if ((existingByNombre.getEmail() == null || existingByNombre.getEmail().isBlank()) && emailNorm != null) {
        existingByNombre.setEmail(emailNorm);
      }
      if ((existingByNombre.getTelefono() == null || existingByNombre.getTelefono().isBlank()) && tel != null && !tel.isBlank()) {
        existingByNombre.setTelefono(tel);
      }
      if (Boolean.FALSE.equals(existingByNombre.isPortalActivo())) {
        existingByNombre.setPortalActivo(true);
      }
      if (existingByNombre.getPasswordHashPortal() == null || existingByNombre.getPasswordHashPortal().isBlank()) {
        existingByNombre.setPasswordHashPortal(encoder.encode(passPortal));
      }

      return repo.save(existingByNombre);
    }

    Cliente c = new Cliente();
    c.setNombre(nombre);
    c.setTelefono(tel);
    c.setEmail(emailNorm);
    c.setPortalActivo(true);
    c.setPasswordHashPortal(encoder.encode(passPortal));

    return repo.save(c);
  }

  private CategoriaEquipo crearCategoriaSiNoExiste(
      CategoriaEquipoRepo repo,
      String codigo,
      String nombre,
      String descripcion,
      String icono,
      int orden
  ) {
    Optional<CategoriaEquipo> existing = repo.findByCodigoIgnoreCase(codigo);
    if (existing.isPresent()) {
      return existing.get();
    }

    CategoriaEquipo c = new CategoriaEquipo();
    c.setCodigo(codigo);
    c.setNombre(nombre);
    c.setDescripcion(descripcion);
    c.setIcono(icono);
    c.setOrdenVisual(orden);
    c.setActiva(true);
    return repo.save(c);
  }

  private void crearFallaSiNoExiste(
      CategoriaEquipoFallaRepo repo,
      CategoriaEquipo categoria,
      String codigo,
      String nombre,
      String descripcion,
      int orden
  ) {
    if (repo.findByCategoriaEquipo_IdAndCodigoIgnoreCase(categoria.getId(), codigo).isPresent()) {
      return;
    }

    CategoriaEquipoFalla f = new CategoriaEquipoFalla();
    f.setCategoriaEquipo(categoria);
    f.setCodigo(codigo);
    f.setNombre(nombre);
    f.setDescripcion(descripcion);
    f.setOrdenVisual(orden);
    f.setActiva(true);
    repo.save(f);
  }

  private void crearEquipoSiNoExiste(
      EquipoRepo equipoRepo,
      Cliente cliente,
      CategoriaEquipo categoria,
      String tipoEquipo,
      String marca,
      String modelo,
      String numeroSerie,
      String ubicacion,
      String descripcion
  ) {
    boolean exists = equipoRepo.findAll().stream()
        .anyMatch(e ->
            e.getCliente() != null &&
            e.getCliente().getId().equals(cliente.getId()) &&
            e.getNumeroSerie() != null &&
            e.getNumeroSerie().equalsIgnoreCase(numeroSerie)
        );

    if (exists) return;

    long next = equipoRepo.count() + 1;
    String codigo = "EQ-" + String.format("%05d", next);
    while (equipoRepo.existsByCodigoEquipoIgnoreCase(codigo)) {
      next++;
      codigo = "EQ-" + String.format("%05d", next);
    }

    Equipo e = new Equipo();
    e.setCliente(cliente);
    e.setCategoriaEquipo(categoria);
    e.setCodigoEquipo(codigo);
    e.setTipoEquipo(tipoEquipo);
    e.setMarca(marca);
    e.setModelo(modelo);
    e.setNumeroSerie(numeroSerie);
    e.setUbicacionHabitual(ubicacion);
    e.setDescripcionGeneral(descripcion);
    e.setEstadoActivo(true);
    equipoRepo.save(e);
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