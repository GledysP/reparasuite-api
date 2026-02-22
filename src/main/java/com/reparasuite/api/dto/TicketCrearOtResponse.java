package com.reparasuite.api.dto;

import java.util.UUID;

public record TicketCrearOtResponse(
    UUID ticketId,
    UUID ordenTrabajoId,
    String ordenTrabajoCodigo
) {}
