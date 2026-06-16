package repository;

import model.producto.Categoria;
import model.producto.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepository implements IProductoRepository {

    public ProductoRepository() {
    }

    @Override
    public void save(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("El producto no puede ser nulo.");
        String sql = "INSERT INTO productos (id, nombre, precio, stock, categoria_nombre) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStock(long id, int nuevoStock) {
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Producto findById(long id) {
        String sql = "SELECT id, nombre, precio, stock, categoria_nombre FROM productos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria categoria = null;
                    String categoriaNombre = rs.getString("categoria_nombre");
                    if (categoriaNombre != null) {
                        categoria = new Categoria(categoriaNombre);
                    }
                    return new Producto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            categoria);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Producto> findAll() {
        String sql = "SELECT id, nombre, precio, stock, categoria_nombre FROM productos";
        List<Producto> productos = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria categoria = null;
                String categoriaNombre = rs.getString("categoria_nombre");
                if (categoriaNombre != null) {
                    categoria = new Categoria(categoriaNombre);
                }
                productos.add(new Producto(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        categoria));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return productos;
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        String sql = "SELECT id, nombre, precio, stock, categoria_nombre FROM productos WHERE LOWER(nombre) LIKE ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categoria categoria = null;
                    String categoriaNombre = rs.getString("categoria_nombre");
                    if (categoriaNombre != null) {
                        categoria = new Categoria(categoriaNombre);
                    }
                    resultado.add(new Producto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            categoria));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultado;
    }

    @Override
    public List<Producto> findByCategoria(String categoria) {
        String sql = "SELECT id, nombre, precio, stock, categoria_nombre FROM productos WHERE categoria_nombre = ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Producto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            new Categoria(rs.getString("categoria_nombre"))));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultado;
    }

    @Override
    public List<Producto> findByRangoPrecio(double min, double max) {
        String sql = "SELECT id, nombre, precio, stock, categoria_nombre FROM productos WHERE precio BETWEEN ? AND ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, min);
            ps.setDouble(2, max);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Producto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            new Categoria(rs.getString("categoria_nombre"))));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultado;
    }
}
