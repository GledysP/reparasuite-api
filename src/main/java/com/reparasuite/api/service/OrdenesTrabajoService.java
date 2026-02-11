package com.reparasuite.api.service;

import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.*;
import com.reparasuite.api.model.*;
import com.reparasuite.api.repo.*;

@Service
public class OrdenesTrabajoService {

  private final OrdenTrabajoRepo otRepo;
  private final ClienteRepo clienteRepo;
  private final UsuarioRepo usuarioRepo;
  private final NotaOtRepo notaRepo;
  private final FotoOtRepo fotoRepo;
  private final TallerRepo tallerRepo;

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public OrdenesTrabajoService(
      OrdenTrabajoRepo otRepo,
      ClienteRepo clienteRepo,
      UsuarioRepo usuarioRepo,
      NotaOtRepo notaRepo,
      FotoOtRepo fotoRepo,
      TallerRepo tallerRepo
  ) {
    this.otRepo = otRepo;
    this.clienteRepo = clienteRepo;
    this.usuarioRepo = usuarioRepo;
    this.notaRepo = notaRepo;
    this.fotoRepo = fotoRepo;
    this.tallerRepo = tallerRepo;
  }

  public ApiListaResponse<OtListaItemDto> listar(String query, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<OrdenTrabajo> p;

    if (query == null || query.isBlank()) {
      p = otRepo.findAll(pageable);
    } else {
      p = otRepo.findByCodigoContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(query, query, pageable);
    }

    return new ApiListaResponse<>(
      p.getContent().stream().map(this::toListaItem).toList(),
      p.getTotalElements()
    );
  }

  public OtDetalleDto obtener(UUID id) {
    OrdenTrabajo ot = otRepo.findById(id).orElseThrow();

    List<NotaDto> notas = notaRepo.findByOt_IdOrderByCreatedAtDesc(id)
        .stream().map(n -> new NotaDto(n.getId(), n.getContenido(), n.getCreatedAt())).toList();

    List<FotoDto> fotos = fotoRepo.findByOt_IdOrderByCreatedAtDesc(id)
        .stream().map(f -> new FotoDto(f.getId(), f.getUrl(), f.getCreatedAt())).toList();

    return toDetalle(ot, notas, fotos);
  }

  public record CrearResponse(UUID id) {}

  public CrearResponse crear(OtCrearRequest req) {
    Cliente cliente;

    if (req.cliente().id() != null && !req.cliente().id().isBlank()) {
      cliente = clienteRepo.findById(UUID.fromString(req.cliente().id())).orElseThrow();
    } else {
      cliente = new Cliente();
      cliente.setNombre(req.cliente().nombre());
      cliente.setTelefono(req.cliente().telefono());
      cliente.setEmail(req.cliente().email());
      cliente = clienteRepo.save(cliente);
    }

    Usuario tecnico = null;
    if (req.tecnicoId() != null && !req.tecnicoId().isBlank()) {
      tecnico = usuarioRepo.findById(UUID.fromString(req.tecnicoId())).orElseThrow();
    }

    Taller t = tallerRepo.findById(1L).orElseThrow();
    String codigo = generarCodigo(t.getPrefijoOt());

    OrdenTrabajo ot = new OrdenTrabajo();
    ot.setCodigo(codigo);
    ot.setCliente(cliente);
    ot.setTecnico(tecnico);
    ot.setTipo(TipoOt.valueOf(req.tipo()));
    ot.setPrioridad(PrioridadOt.valueOf(req.prioridad()));
    ot.setDescripcion(req.descripcion());
    ot.setEstado(EstadoOt.RECIBIDA);

    if (req.fechaPrevista() != null && !req.fechaPrevista().isBlank()) {
      ot.setFechaPrevista(OffsetDateTime.parse(req.fechaPrevista()));
    }
    ot.setDireccion(req.direccion());
    ot.setNotasAcceso(req.notasAcceso());

    ot = otRepo.save(ot);
    return new CrearResponse(ot.getId());
  }

  public void cambiarEstado(UUID id, String estado) {
    OrdenTrabajo ot = otRepo.findById(id).orElseThrow();
    ot.setEstado(EstadoOt.valueOf(estado));
    otRepo.save(ot);
  }

  public void anadirNota(UUID id, String contenido) {
    OrdenTrabajo ot = otRepo.findById(id).orElseThrow();
    NotaOt n = new NotaOt();
    n.setOt(ot);
    n.setContenido(contenido);
    notaRepo.save(n);
  }

  public FotoDto subirFoto(UUID id, MultipartFile file) throws IOException {
    OrdenTrabajo ot = otRepo.findById(id).orElseThrow();

    Files.createDirectories(Paths.get(uploadDir));
    String filename = "ot-" + id + "-" + UUID.randomUUID() + "-" + safeName(file.getOriginalFilename());
    Path path = Paths.get(uploadDir).resolve(filename);
    Files.write(path, file.getBytes(), StandardOpenOption.CREATE_NEW);

    String url = "/files/" + filename;

    FotoOt f = new FotoOt();
    f.setOt(ot);
    f.setUrl(url);
    f = fotoRepo.save(f);

    return new FotoDto(f.getId(), f.getUrl(), f.getCreatedAt());
  }

  private String safeName(String n) {
    if (n == null) return "foto.bin";
    return n.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String generarCodigo(String prefijo) {
    // Simple y suficiente para demo
    long num = System.currentTimeMillis() % 100000;
    return prefijo + "-" + String.format("%05d", num);
  }

  private OtListaItemDto toListaItem(OrdenTrabajo ot) {
    return new OtListaItemDto(
      ot.getId(),
      ot.getCodigo(),
      ot.getEstado().name(),
      ot.getTipo().name(),
      ot.getPrioridad().name(),
      ot.getCliente().getNombre(),
      ot.getTecnico() != null ? ot.getTecnico().getNombre() : null,
      ot.getUpdatedAt()
    );
  }

  private OtDetalleDto toDetalle(OrdenTrabajo ot, List<NotaDto> notas, List<FotoDto> fotos) {
    Cliente c = ot.getCliente();
    Usuario tec = ot.getTecnico();

    ClienteResumenDto clienteDto = new ClienteResumenDto(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail());
    UsuarioResumenDto tecnicoDto = tec == null ? null :
      new UsuarioResumenDto(tec.getId(), tec.getNombre(), tec.getUsuario(), tec.getRol().name(), tec.isActivo());

    return new OtDetalleDto(
      ot.getId(),
      ot.getCodigo(),
      ot.getEstado().name(),
      ot.getTipo().name(),
      ot.getPrioridad().name(),
      ot.getDescripcion(),
      clienteDto,
      tecnicoDto,
      ot.getFechaPrevista(),
      ot.getDireccion(),
      ot.getNotasAcceso(),
      notas,
      fotos,
      ot.getCreatedAt(),
      ot.getUpdatedAt()
    );
  }
}
