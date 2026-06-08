package repository;

import model.producto.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoRepository implements IProductoRepository {
    private List<Producto> productos;

    public ProductoRepository() {
        productos = new ArrayList<>();
    }

    @Override
    public void save(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("El producto no puede ser nulo.");
        productos.add(producto);
    }

    @Override
    public Producto findById(long id) {
        for (Producto p : productos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos);
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public List<Producto> findByCategoria(String categoria) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCategoria() != null
                    && p.getCategoria().getNombre().equalsIgnoreCase(categoria)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public List<Producto> findByRangoPrecio(double min, double max) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getPrecio() >= min && p.getPrecio() <= max) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}
