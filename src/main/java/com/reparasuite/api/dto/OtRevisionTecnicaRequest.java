package com.reparasuite.api.dto;

public record OtRevisionTecnicaRequest(
    String fallaDetectada,
    String diagnosticoTecnico,
    String trabajoARealizar
) { }