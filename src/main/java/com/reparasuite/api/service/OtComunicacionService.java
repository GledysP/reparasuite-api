package com.reparasuite.api.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.dto.FotoDto;
import com.reparasuite.api.dto.MensajeDto;
import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.ForbiddenException;
import com.reparasuite.api.exception.NotFoundException;
import com.reparasuite.api.model.ActorTipo;
import com.reparasuite.api.model.Cliente;
import com.reparasuite.api.model.EventoHistorialOt;
import com.reparasuite.api.model.FotoOt;
import com.reparasuite.api.model.HistorialOt;
import com.reparasuite.api.model.MensajeOt;
import com.reparasuite.api.model.NotaOt;
import com.reparasuite.api.model.OrdenTrabajo;
import com.reparasuite.api.model.TipoRemitente;
import com.reparasuite.api.model.Usuario;
import com.reparasuite.api.repo.ClienteRepo;
import com.reparasuite.api.repo.FotoOtRepo;
import com.reparasuite.api.repo.HistorialOtRepo;
import com.reparasuite.api.repo.MensajeOtRepo;
import com.reparasuite.api.repo.NotaOtRepo;
import com.reparasuite.api.repo.OrdenTrabajoRepo;
import com.reparasuite.api.repo.UsuarioRepo;

@Service
public class OtComunicacionService {

    private final OrdenTrabajoRepo otRepo;
    private final NotaOtRepo notaRepo;
    private final FotoOtRepo fotoRepo;
    private final MensajeOtRepo mensajeRepo;
    private final HistorialOtRepo historialRepo;
    private final SecureUploadService secureUploadService;
    private final AuthContextService authContextService;
    private final ClienteRepo clienteRepo;
    private final UsuarioRepo usuarioRepo;

    public OtComunicacionService(
            OrdenTrabajoRepo otRepo,
            NotaOtRepo notaRepo,
            FotoOtRepo fotoRepo,
            MensajeOtRepo mensajeRepo,
            HistorialOtRepo historialRepo,
            SecureUploadService secureUploadService,
            AuthContextService authContextService,
            ClienteRepo clienteRepo,
            UsuarioRepo usuarioRepo) {
        this.otRepo = otRepo;
        this.notaRepo = notaRepo;
        this.fotoRepo = fotoRepo;
        this.mensajeRepo = mensajeRepo;
        this.historialRepo = historialRepo;
        this.secureUploadService = secureUploadService;
        this.authContextService = authContextService;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public void anadirNota(String idOrCodigo, String contenido, boolean visibleCliente) {
        OrdenTrabajo ot = resolverOt(idOrCodigo);

        if (authContextService.current().isCliente()) {
            ensureClienteOwner(ot);
            visibleCliente = true;
        } else if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }

        NotaOt n = new NotaOt();
        n.setOt(ot);
        n.setContenido(contenido);
        n.setVisibleCliente(visibleCliente);
        notaRepo.save(n);

        registrarEvento(
                ot,
                EventoHistorialOt.NOTA_AGREGADA,
                visibleCliente ? "Se añadió una nota visible al cliente" : "Se añadió una nueva nota interna"
        );
    }

    @Transactional
    public FotoDto subirFoto(String idOrCodigo, MultipartFile file, boolean visibleCliente) throws IOException {
        OrdenTrabajo ot = resolverOt(idOrCodigo);

        if (authContextService.current().isCliente()) {
            ensureClienteOwner(ot);
            visibleCliente = true;
        } else if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }

        var stored = secureUploadService.storeOtImage(ot.getId(), file);

        FotoOt f = new FotoOt();
        f.setOt(ot);
        f.setUrl(stored.url());
        f.setVisibleCliente(visibleCliente);
        f = fotoRepo.save(f);

        registrarEvento(ot, EventoHistorialOt.FOTO_SUBIDA, "Archivo subido: " + stored.originalFilename());

        return new FotoDto(f.getId(), f.getUrl(), f.getCreatedAt());
    }

    @Transactional
    public MensajeDto enviarMensaje(String idOrCodigo, String contenido) {
        OrdenTrabajo ot = resolverOt(idOrCodigo);

        if (authContextService.current().isCliente()) {
            ensureClienteOwner(ot);
        } else if (!authContextService.current().isBackoffice()) {
            throw new ForbiddenException("No autorizado");
        }

        MensajeOt m = new MensajeOt();
        m.setOt(ot);

        if (authContextService.current().isCliente()) {
            Cliente c = clienteRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
            m.setRemitenteTipo(TipoRemitente.CLIENTE);
            m.setRemitenteNombre(c.getNombre());
        } else {
            Usuario u = usuarioRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            m.setRemitenteTipo(TipoRemitente.USUARIO);
            m.setRemitenteNombre(u.getNombre());
        }

        m.setContenido(contenido);
        m = mensajeRepo.save(m);

        registrarEvento(ot, EventoHistorialOt.MENSAJE_ENVIADO, "Mensaje enviado");
        return new MensajeDto(
                m.getId(),
                m.getRemitenteTipo().name(),
                m.getRemitenteNombre(),
                m.getContenido(),
                m.getCreatedAt()
        );
    }

    // --- Utilidades ---

    private OrdenTrabajo resolverOt(String idOrCodigo) {
        if (idOrCodigo == null || idOrCodigo.isBlank()) {
            throw new BadRequestException("ID de orden inválido");
        }
        try {
            UUID id = UUID.fromString(idOrCodigo);
            return otRepo.findById(id).orElseThrow(() -> new NotFoundException("Orden no encontrada"));
        } catch (IllegalArgumentException ex) {
            return otRepo.findByCodigoIgnoreCase(idOrCodigo.trim())
                    .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
        }
    }

    private void registrarEvento(OrdenTrabajo ot, EventoHistorialOt evento, String descripcion) {
        HistorialOt h = new HistorialOt();
        h.setOt(ot);
        h.setEvento(evento);
        h.setDescripcion(descripcion);

        if (authContextService.current().isCliente()) {
            Cliente c = clienteRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
            h.setActorTipo(ActorTipo.CLIENTE);
            h.setActorNombre(c.getNombre());
            h.setUsuario(null);
        } else {
            Usuario u = usuarioRepo.findById(authContextService.current().id())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            h.setActorTipo(ActorTipo.USUARIO);
            h.setActorNombre(u.getNombre());
            h.setUsuario(u);
        }
        historialRepo.save(h);
    }

    private void ensureClienteOwner(OrdenTrabajo ot) {
        if (!authContextService.current().isCliente() || !ot.getCliente().getId().equals(authContextService.current().id())) {
            throw new ForbiddenException("No autorizado");
        }
    }
}