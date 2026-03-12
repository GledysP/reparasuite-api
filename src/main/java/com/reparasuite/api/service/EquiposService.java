package com.reparasuite.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.ApiListaResponse;
import com.reparasuite.api.dto.CategoriaEquipoDto;
import com.reparasuite.api.dto.CategoriaEquipoFallaDto;
import com.reparasuite.api.dto.CategoriaEquipoGuardarRequest;
import com.reparasuite.api.dto.EquipoCrearRequest;
import com.reparasuite.api.dto.EquipoDetalleDto;
import com.reparasuite.api.dto.EquipoResumenDto;
import com.reparasuite.api.model.CategoriaEquipo;
import com.reparasuite.api.model.CategoriaEquipoFalla;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.Equipo;
import com.reparasuite.api.repo.CategoriaEquipoFallaRepo;
import com.reparasuite.api.repo.CategoriaEquipoRepo;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.EquipoRepo;

@Service
public class EquiposService {

  private final EquipoRepo equipoRepo;
  private final ClienteRepo clienteRepo;
  private final CategoriaEquipoRepo categoriaRepo;
  private final CategoriaEquipoFallaRepo fallaRepo;

  public EquiposService(
      EquipoRepo equipoRepo,
      ClienteRepo clienteRepo,
      CategoriaEquipoRepo categoriaRepo,
      CategoriaEquipoFallaRepo fallaRepo
  ) {
    this.equipoRepo = equipoRepo;
    this.clienteRepo = clienteRepo;
    this.categoriaRepo = categoriaRepo;
    this.fallaRepo = fallaRepo;
  }

  public ApiListaResponse<EquipoResumenDto> listar(String clienteId, Boolean activo, int page, int size) {
    Pageable pageable = PageRequest.of(
        Math.max(page, 0),
        Math.max(size, 1),
        Sort.by(Sort.Direction.ASC, "codigoEquipo")
    );

    Page<Equipo> p;
    if (clienteId != null && !clienteId.isBlank()) {
      UUID cid = UUID.fromString(clienteId);
      if (activo == null) {
        p = equipoRepo.findByCliente_Id(cid, pageable);
      } else {
        p = equipoRepo.findByCliente_IdAndEstadoActivo(cid, activo, pageable);
      }
    } else if (activo != null) {
      if (activo) {
        p = equipoRepo.findByEstadoActivoTrue(pageable);
      } else {
        p = equipoRepo.findAll(pageable);
      }
    } else {
      p = equipoRepo.findAll(pageable);
    }

    return new ApiListaResponse<>(
        p.getContent().stream().map(this::toResumen).toList(),
        p.getTotalElements()
    );
  }

  public EquipoDetalleDto obtener(UUID id) {
    Equipo e = equipoRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    return toDetalle(e);
  }

  @Transactional
  public EquipoDetalleDto crear(EquipoCrearRequest req) {
    Cliente cliente = clienteRepo.findById(UUID.fromString(req.clienteId()))
        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    CategoriaEquipo categoria = resolveCategoria(req.categoriaEquipoId());

    Equipo e = new Equipo();
    e.setCliente(cliente);
    e.setCategoriaEquipo(categoria);
    e.setCodigoEquipo(generarCodigoEquipo());
    e.setCodigoInterno(limpiarNullable(req.codigoInterno()));
    e.setTipoEquipo(limpiarNullable(req.tipoEquipo()));
    e.setMarca(limpiarNullable(req.marca()));
    e.setModelo(limpiarNullable(req.modelo()));
    e.setNumeroSerie(limpiarNullable(req.numeroSerie()));
    e.setDescripcionGeneral(limpiarNullable(req.descripcionGeneral()));
    e.setFechaCompra(parseDateNullable(req.fechaCompra()));
    e.setGarantiaHasta(parseDateNullable(req.garantiaHasta()));
    e.setUbicacionHabitual(limpiarNullable(req.ubicacionHabitual()));
    e.setNotasTecnicas(limpiarNullable(req.notasTecnicas()));
    e.setEstadoActivo(req.estadoActivo() == null ? true : req.estadoActivo());

    e = equipoRepo.save(e);
    return toDetalle(e);
  }

  @Transactional
  public EquipoDetalleDto actualizar(UUID id, EquipoCrearRequest req) {
    Equipo e = equipoRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

    Cliente cliente = clienteRepo.findById(UUID.fromString(req.clienteId()))
        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    CategoriaEquipo categoria = resolveCategoria(req.categoriaEquipoId());

    e.setCliente(cliente);
    e.setCategoriaEquipo(categoria);
    e.setCodigoInterno(limpiarNullable(req.codigoInterno()));
    e.setTipoEquipo(limpiarNullable(req.tipoEquipo()));
    e.setMarca(limpiarNullable(req.marca()));
    e.setModelo(limpiarNullable(req.modelo()));
    e.setNumeroSerie(limpiarNullable(req.numeroSerie()));
    e.setDescripcionGeneral(limpiarNullable(req.descripcionGeneral()));
    e.setFechaCompra(parseDateNullable(req.fechaCompra()));
    e.setGarantiaHasta(parseDateNullable(req.garantiaHasta()));
    e.setUbicacionHabitual(limpiarNullable(req.ubicacionHabitual()));
    e.setNotasTecnicas(limpiarNullable(req.notasTecnicas()));
    e.setEstadoActivo(req.estadoActivo() == null ? e.isEstadoActivo() : req.estadoActivo());

    e = equipoRepo.save(e);
    return toDetalle(e);
  }

  @Transactional
  public void desactivar(UUID id) {
    Equipo e = equipoRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    e.setEstadoActivo(false);
    equipoRepo.save(e);
  }

  public List<CategoriaEquipoDto> categorias() {
    return categoriaRepo.findByActivaTrueOrderByOrdenVisualAscNombreAsc().stream()
        .map(this::toCategoriaDto)
        .toList();
  }

  @Transactional
  public CategoriaEquipoDto crearCategoria(CategoriaEquipoGuardarRequest req) {
    String codigo = normalizeCode(req.codigo());
    if (categoriaRepo.findByCodigoIgnoreCase(codigo).isPresent()) {
      throw new RuntimeException("Ya existe una categoría con ese código");
    }

    CategoriaEquipo c = new CategoriaEquipo();
    c.setCodigo(codigo);
    c.setNombre(cleanRequired(req.nombre(), "nombre"));
    c.setDescripcion(limpiarNullable(req.descripcion()));
    c.setIcono(limpiarNullable(req.icono()));
    c.setOrdenVisual(req.ordenVisual() == null ? 0 : req.ordenVisual());
    c.setActiva(req.activa() == null ? true : req.activa());

    c = categoriaRepo.save(c);
    return toCategoriaDto(c);
  }

  @Transactional
  public CategoriaEquipoDto actualizarCategoria(UUID id, CategoriaEquipoGuardarRequest req) {
    CategoriaEquipo c = categoriaRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

    String codigo = normalizeCode(req.codigo());
    categoriaRepo.findByCodigoIgnoreCase(codigo).ifPresent(existing -> {
      if (!existing.getId().equals(id)) {
        throw new RuntimeException("Ya existe una categoría con ese código");
      }
    });

    c.setCodigo(codigo);
    c.setNombre(cleanRequired(req.nombre(), "nombre"));
    c.setDescripcion(limpiarNullable(req.descripcion()));
    c.setIcono(limpiarNullable(req.icono()));
    c.setOrdenVisual(req.ordenVisual() == null ? 0 : req.ordenVisual());
    c.setActiva(req.activa() == null ? c.isActiva() : req.activa());

    c = categoriaRepo.save(c);
    return toCategoriaDto(c);
  }

  public List<CategoriaEquipoFallaDto> fallasPorCategoria(UUID categoriaId) {
    return fallaRepo.findByCategoriaEquipo_IdAndActivaTrueOrderByOrdenVisualAscNombreAsc(categoriaId).stream()
        .map(this::toFallaDto)
        .toList();
  }

  private CategoriaEquipo resolveCategoria(String categoriaId) {
    if (categoriaId == null || categoriaId.isBlank()) return null;

    return categoriaRepo.findById(UUID.fromString(categoriaId))
        .orElseThrow(() -> new RuntimeException("Categoría de equipo no encontrada"));
  }

  private EquipoResumenDto toResumen(Equipo e) {
    return new EquipoResumenDto(
        e.getId(),
        e.getCodigoEquipo(),
        e.getCliente().getId(),
        e.getCliente().getNombre(),
        e.getCategoriaEquipo() != null ? e.getCategoriaEquipo().getId() : null,
        e.getCategoriaEquipo() != null ? e.getCategoriaEquipo().getNombre() : null,
        e.getTipoEquipo(),
        e.getMarca(),
        e.getModelo(),
        e.getNumeroSerie(),
        e.getUbicacionHabitual(),
        e.isEstadoActivo()
    );
  }

  private EquipoDetalleDto toDetalle(Equipo e) {
    return new EquipoDetalleDto(
        e.getId(),
        e.getCodigoEquipo(),
        e.getCliente().getId(),
        e.getCliente().getNombre(),
        e.getCategoriaEquipo() != null ? toCategoriaDto(e.getCategoriaEquipo()) : null,
        e.getCodigoInterno(),
        e.getTipoEquipo(),
        e.getMarca(),
        e.getModelo(),
        e.getNumeroSerie(),
        e.getDescripcionGeneral(),
        e.getFechaCompra() != null ? e.getFechaCompra().toString() : null,
        e.getGarantiaHasta() != null ? e.getGarantiaHasta().toString() : null,
        e.getUbicacionHabitual(),
        e.getNotasTecnicas(),
        e.isEstadoActivo(),
        e.getCreatedAt(),
        e.getUpdatedAt()
    );
  }

  private CategoriaEquipoDto toCategoriaDto(CategoriaEquipo c) {
    return new CategoriaEquipoDto(
        c.getId(),
        c.getCodigo(),
        c.getNombre(),
        c.getDescripcion(),
        c.getIcono()
    );
  }

  private CategoriaEquipoFallaDto toFallaDto(CategoriaEquipoFalla f) {
    return new CategoriaEquipoFallaDto(
        f.getId(),
        f.getCodigo(),
        f.getNombre(),
        f.getDescripcion()
    );
  }

  private LocalDate parseDateNullable(String raw) {
    if (raw == null || raw.isBlank()) return null;
    return LocalDate.parse(raw.trim());
  }

  private String limpiarNullable(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isBlank() ? null : t;
  }

  private String cleanRequired(String value, String fieldName) {
    String x = limpiarNullable(value);
    if (x == null) {
      throw new RuntimeException("El campo " + fieldName + " es obligatorio");
    }
    return x;
  }

  private String normalizeCode(String code) {
    String x = cleanRequired(code, "codigo").toUpperCase();
    return x.replace(' ', '_');
  }

  private String generarCodigoEquipo() {
    long total = equipoRepo.count() + 1;
    String codigo = "EQ-" + String.format("%05d", total);

    while (equipoRepo.existsByCodigoEquipoIgnoreCase(codigo)) {
      total++;
      codigo = "EQ-" + String.format("%05d", total);
    }

    return codigo;
  }
}