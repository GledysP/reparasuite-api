package com.reparasuite.api.dto;

public record TallerDto(
    String nombre,
    String rif,
    String telefono,
    String email,
    String direccion,
    String prefijoOt
) { }
