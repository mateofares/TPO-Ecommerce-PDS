package com.emarket.service;

import com.emarket.dto.LoginRequest;
import com.emarket.dto.LoginResponse;
import com.emarket.dto.RegistroAdminRequest;
import com.emarket.dto.RegistroClienteRequest;
import com.emarket.model.usuario.Administrador;
import com.emarket.model.usuario.Cliente;
import com.emarket.model.usuario.Usuario;
import com.emarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Cliente registrarCliente(RegistroClienteRequest req) {
        if (usuarioRepository.existsByEmailIgnoreCase(req.email()))
            throw new IllegalArgumentException("Ya existe una cuenta con el email: " + req.email());

        Cliente cliente = new Cliente(req.nombre(), req.apellido(), req.email(), req.contrasenia());
        return (Cliente) usuarioRepository.save(cliente);
    }

    public Administrador registrarAdministrador(RegistroAdminRequest req) {
        if (usuarioRepository.existsByEmailIgnoreCase(req.email()))
            throw new IllegalArgumentException("Ya existe una cuenta con el email: " + req.email());

        Administrador admin = new Administrador(
                req.nombre(), req.apellido(), req.email(), req.contrasenia(), req.legajo());
        return (Administrador) usuarioRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas."));

        if (!usuario.validarCredenciales(req.email(), req.contrasenia()))
            throw new IllegalArgumentException("Credenciales incorrectas.");

        return new LoginResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarCliente(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        if (!(u instanceof Cliente))
            throw new IllegalArgumentException("El usuario #" + id + " no es un cliente.");
        return (Cliente) u;
    }
}
