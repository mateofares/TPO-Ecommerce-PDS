package service;

import model.usuario.Usuario;

public interface IUsuarioService {
    Usuario registrarCliente(String nombre, String apellido, String email, String contrasenia);
    Usuario registrarAdministrador(String nombre, String apellido, String email, String contrasenia, int legajo);
    Usuario login(String email, String contrasenia);
}
