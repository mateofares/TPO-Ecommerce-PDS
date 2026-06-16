package com.emarket.dto;

import com.emarket.state.EstadoPedidoNombre;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoRequest(
        @NotNull EstadoPedidoNombre estado
) {}
