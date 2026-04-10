package com.reparasuite.api.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCrearRequest(

    // Se mantiene por compatibilidad con  frontend actual
    @NotBlank
    @Size(max = 200)
    String asunto,

    // Se mantiene por compatibilidad con  frontend actual
    @NotBlank
    @Size(max = 4000)
    String descripcion,

    // ✅ NUEVOS CAMPOS ESTRUCTURADOS (opcionales en esta fase para no romper)
    @Size(max = 200)
    String equipo,

    @Size(max = 4000)
    String descripcionFalla,

    // opcional: lo decide backoffice, pero cliente puede sugerir
    // valores sugeridos: "TIENDA" / "DOMICILIO" / null
    @Size(max = 20)
    String tipoServicioSugerido,

    @Size(max = 500)
    String direccion,
    
    @Size(max = 4000)
    String observaciones,

    List<String> categoriasTrabajo // ✅ NUEVO CAMPO PARA LOS CHIPS
) { }