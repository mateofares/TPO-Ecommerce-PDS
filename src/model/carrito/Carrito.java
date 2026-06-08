package model.carrito;

import model.producto.Producto;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private int usuarioId;
    private int carritoId;
    private List<ItemCarrito> items;

    public Carrito(int usuarioId) {
        this.usuarioId = usuarioId;
        this.items = new ArrayList<>();
    }

    public void agregar(Producto producto, int cantidad) {
        if (!producto.hayStock(cantidad))
            throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                item.modificarCantidad(item.getCantidad() + cantidad);
                return;
            }
        }
        items.add(new ItemCarrito(producto, cantidad));
    }

    public void modificarCantidad(long productoId, int nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId() == productoId) {
                if (nuevaCantidad <= 0) {
                    items.remove(item);
                } else {
                    item.modificarCantidad(nuevaCantidad);
                }
                return;
            }
        }
    }

    public void eliminarItem(long productoId) {
        items.removeIf(i -> i.getProducto().getId() == productoId);
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.calcularPrecioItem();
        }
        return total;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void vaciar() {
        items.clear();
    }

    public List<ItemCarrito> getItems() { return items; }
    public int getUsuarioId() { return usuarioId; }
    public int getCarritoId() { return carritoId; }
}
