package service;

import model.usuario.Usuario;

public interface IUsuarioService {
    void registrarCliente(String nombre, String apellido, String email, String contrasenia);
    void registrarAdministrador(String nombre, String apellido, String email, String contrasenia, int legajo);
    Usuario login(String email, String contrasenia);
}
