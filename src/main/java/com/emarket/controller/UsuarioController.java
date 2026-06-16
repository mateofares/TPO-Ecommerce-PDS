package com.emarket.controller;

import com.emarket.dto.LoginRequest;
import com.emarket.dto.LoginResponse;
import com.emarket.dto.RegistroAdminRequest;
import com.emarket.dto.RegistroClienteRequest;
import com.emarket.model.usuario.Administrador;
import com.emarket.model.usuario.Cliente;
import com.emarket.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro/cliente")
    public ResponseEntity<?> registrarCliente(@Valid @RequestBody RegistroClienteRequest req) {
        try {
            Cliente cliente = usuarioService.registrarCliente(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", cliente.getId(), "nombre", cliente.getNombre(),
                                 "email", cliente.getEmail(), "rol", "CLIENTE"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registro/admin")
    public ResponseEntity<?> registrarAdmin(@Valid @RequestBody RegistroAdminRequest req) {
        try {
            Administrador admin = usuarioService.registrarAdministrador(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", admin.getId(), "nombre", admin.getNombre(),
                                 "email", admin.getEmail(), "rol", "ADMINISTRADOR",
                                 "legajo", admin.getLegajo()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse response = usuarioService.login(req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
