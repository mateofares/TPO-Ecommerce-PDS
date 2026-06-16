package com.emarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuscribirObservadorRequest(
        /** Tipo de observador: EMAIL, SMS, PUSH */
        @NotNull String tipo,
        /** Destino: dirección email, número de teléfono o deviceId */
        @NotBlank String destino
) {}
