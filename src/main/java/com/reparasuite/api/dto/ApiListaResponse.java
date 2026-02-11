package com.reparasuite.api.dto;

import java.util.List;

public record ApiListaResponse<T>(List<T> items, long total) { }
