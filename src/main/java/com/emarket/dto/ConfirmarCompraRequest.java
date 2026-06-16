package com.emarket.dto;

import com.emarket.strategy.TipoPago;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// los campos de pago varían según el método elegido, los que no aplican se mandan null
public record ConfirmarCompraRequest(
        @NotNull Long clienteId,
        @NotNull TipoPago tipoPago,
        @NotNull @NotEmpty List<ItemCompraRequest> items,
        // Tarjeta de crédito / débito
        String numero,
        String cvv,
        // PayPal
        String cuenta,
        String token,
        // Transferencia
        String cbu,
        String banco
) {}
