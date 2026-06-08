package repository;

import model.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository implements IUsuarioRepository {
    private List<Usuario> usuarios;

    public UsuarioRepository() {
        usuarios = new ArrayList<>();
    }

    @Override
    public void save(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("El usuario no puede ser nulo.");
        usuarios.add(usuario);
    }

    @Override
    public Usuario findByEmail(String email) {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    @Override
    public boolean existeEmail(String email) {
        return findByEmail(email) != null;
    }

    public List<Usuario> findAll() {
        return new ArrayList<>(usuarios);
    }
}
