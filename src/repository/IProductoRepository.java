package repository;

import model.producto.Producto;

import java.util.List;

public interface IProductoRepository {
    void save(Producto producto);
    Producto findById(long id);
    List<Producto> findAll();
    List<Producto> findByNombre(String nombre);
    List<Producto> findByCategoria(String categoria);
    List<Producto> findByRangoPrecio(double min, double max);
}
