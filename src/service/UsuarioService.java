package service;

import model.usuario.Administrador;
import model.usuario.Cliente;
import model.usuario.Usuario;
import repository.IUsuarioRepository;

public class UsuarioService implements IUsuarioService {
    private IUsuarioRepository usuarioRepository;
    private long nextId = 1;

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void registrarCliente(String nombre, String apellido, String email, String contrasenia) {
        if (usuarioRepository.existeEmail(email))
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        Cliente cliente = new Cliente(nextId++, nombre, apellido, email, contrasenia);
        usuarioRepository.save(cliente);
        System.out.println("Cliente registrado: " + nombre + " " + apellido);
    }

    @Override
    public void registrarAdministrador(String nombre, String apellido, String email, String contrasenia, int legajo) {
        if (usuarioRepository.existeEmail(email))
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        Administrador admin = new Administrador(nextId++, nombre, apellido, email, contrasenia, legajo);
        usuarioRepository.save(admin);
        System.out.println("Administrador registrado: " + nombre + " " + apellido + " (Legajo: " + legajo + ")");
    }

    @Override
    public Usuario login(String email, String contrasenia) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !usuario.login(email, contrasenia)) {
            throw new IllegalArgumentException("Credenciales incorrectas.");
        }
        System.out.println("Sesión iniciada: " + usuario.getNombre() + " (" + usuario.getClass().getSimpleName() + ")");
        return usuario;
    }
}
