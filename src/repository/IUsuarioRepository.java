package repository;

import model.usuario.Usuario;

public interface IUsuarioRepository {
    void save(Usuario usuario);
    Usuario findById(long id);
    Usuario findByEmail(String email);
    boolean existeEmail(String email);
}
