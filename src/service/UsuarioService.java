package service;

import model.usuario.Administrador;
import model.usuario.Cliente;
import model.usuario.Usuario;
import repository.IUsuarioRepository;

public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario registrarCliente(String nombre, String apellido, String email, String contrasenia) {
        if (usuarioRepository.existeEmail(email))
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        long id = System.nanoTime() / 1000000;
        Cliente cliente = new Cliente(id, nombre, apellido, email, contrasenia);
        usuarioRepository.save(cliente);
        return cliente;
    }

    @Override
    public Usuario registrarAdministrador(String nombre, String apellido, String email, String contrasenia, int legajo) {
        if (usuarioRepository.existeEmail(email))
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        long id = System.nanoTime() / 1000000;
        Administrador admin = new Administrador(id, nombre, apellido, email, contrasenia, legajo);
        usuarioRepository.save(admin);
        return admin;
    }

    @Override
    public Usuario login(String email, String contrasenia) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !usuario.getContrasenia().equals(contrasenia))
            throw new IllegalArgumentException("Email o contrasena incorrectos.");
        return usuario;
    }
}
