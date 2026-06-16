package repository;

import model.usuario.Administrador;
import model.usuario.Cliente;
import model.usuario.Usuario;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioRepository implements IUsuarioRepository {

    public UsuarioRepository() {
    }

    @Override
    public void save(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("El usuario no puede ser nulo.");
        String sql = "INSERT INTO usuarios (id, nombre, apellido, email, contrasenia, rol) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuario.getId());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getApellido());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getContrasenia());
            ps.setString(6, usuario instanceof Administrador ? "ADMIN" : "CLIENTE");
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario findByEmail(String email) {
        String sql = "SELECT id, nombre, apellido, email, contrasenia, rol FROM usuarios WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    String contrasenia = rs.getString("contrasenia");
                    String rol = rs.getString("rol");
                    if ("ADMIN".equalsIgnoreCase(rol)) {
                        return new Administrador(id, nombre, apellido, email, contrasenia, 0);
                    }
                    return new Cliente(id, nombre, apellido, email, contrasenia);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}
