package com.emarket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroClienteRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @Email @NotBlank String email,
        @Size(min = 4) @NotBlank String contrasenia
) {}
