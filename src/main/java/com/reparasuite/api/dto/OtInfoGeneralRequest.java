package com.reparasuite.api.dto;

import java.util.List;

public record OtInfoGeneralRequest(
    String tipo,             // Modalidad (TIENDA/DOMICILIO)
    String prioridad,        // ALTA/MEDIA/BAJA
    String tecnicoId,        // UUID del técnico
    String direccion,        // Ubicación
    String notasAcceso,      // Notas de acceso
    List<String> categoriasTrabajo // Cambiar los servicios a realizar
) { }