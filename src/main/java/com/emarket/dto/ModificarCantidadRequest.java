package com.emarket.dto;

import jakarta.validation.constraints.Min;

public record ModificarCantidadRequest(
        @Min(0) int cantidad
) {}
