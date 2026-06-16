package com.emarket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCompraRequest(
        @NotNull Long productoId,
        @Min(1) int cantidad
) {}
