package com.reparasuite.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PagoDto(
    UUID id,
    String estado,
    BigDecimal importe,
    String comprobanteUrl
) { }
