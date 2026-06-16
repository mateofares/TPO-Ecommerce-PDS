package com.emarket.dto;

public record LoginResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String rol
) {}
