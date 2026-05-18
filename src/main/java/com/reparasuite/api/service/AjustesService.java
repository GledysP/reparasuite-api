package com.reparasuite.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.dto.TallerDto;
import com.reparasuite.api.model.Taller;
import com.reparasuite.api.repo.TallerRepo;

@Service
public class AjustesService {

    private static final Logger log = LoggerFactory.getLogger(AjustesService.class);
    private static final Long MASTER_TALLER_ID = 1L;

    private final TallerRepo tallerRepo;

    public AjustesService(TallerRepo tallerRepo) {
        this.tallerRepo = tallerRepo;
    }

    /**
     * Obtiene la configuración global. 
     * Si no existe en BD, devuelve un objeto por defecto para que el Front no explote.
     */
    @Transactional(readOnly = true)
    public TallerDto obtenerTaller() {
        return tallerRepo.findById(MASTER_TALLER_ID)
            .map(t -> new TallerDto(
                t.getNombre(),
                t.getRif(), // Asegúrate de tener este campo en el DTO
                t.getTelefono(),
                t.getEmail(),
                t.getDireccion(),
                t.getPrefijoOt()
            ))
            .orElseGet(() -> {
                log.info("Taller no encontrado en BD. Devolviendo configuración inicial por defecto.");
                return new TallerDto("", "", "", "", "", "OT-");
            });
    }

    /**
     * Lógica "Upsert" (Update or Insert).
     * Garantiza que el registro ID 1 siempre exista tras la primera interacción.
     */
    @Transactional
    public void guardarTaller(TallerDto dto) {
        log.info("Iniciando guardado de configuración para Taller ID: {}", MASTER_TALLER_ID);

        Taller taller = tallerRepo.findById(MASTER_TALLER_ID).orElseGet(() -> {
            log.info("Creando nuevo registro maestro de taller (ID 1)");
            Taller nuevo = new Taller();
            nuevo.setId(MASTER_TALLER_ID);
            nuevo.setPrefijoOt("OT-"); // Valor operativo crítico por defecto
            return nuevo;
        });

        // Mapeo de datos desde DTO a Entidad
        taller.setNombre(dto.nombre());
        taller.setRif(dto.rif()); // Asegúrate de tener el setter en el modelo Taller
        taller.setTelefono(dto.telefono());
        taller.setEmail(dto.email());
        taller.setDireccion(dto.direccion());

        // Solo actualizamos el prefijo si el DTO trae uno válido
        if (dto.prefijoOt() != null && !dto.prefijoOt().isBlank()) {
            taller.setPrefijoOt(dto.prefijoOt().trim().toUpperCase());
        }

        try {
            tallerRepo.save(taller);
            log.info("Configuración de taller guardada exitosamente.");
        } catch (Exception e) {
            log.error("Error crítico al persistir la configuración del taller: {}", e.getMessage());
            throw e; 
        }
    }
}