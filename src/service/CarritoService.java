package service;

import model.carrito.Carrito;
import model.carrito.ItemCarrito;
import model.producto.Producto;

public class CarritoService implements ICarritoService {
    private Carrito carrito;

    public CarritoService(Carrito carrito) {
        this.carrito = carrito;
    }

    @Override
    public void agregarProducto(Producto producto, int cantidad) {
        carrito.agregar(producto, cantidad);
        System.out.println("Agregado: " + producto.getNombre() + " x" + cantidad);
    }

    @Override
    public void modificarCantidad(long productoId, int nuevaCantidad) {
        carrito.modificarCantidad(productoId, nuevaCantidad);
    }

    @Override
    public void eliminarItem(long productoId) {
        carrito.eliminarItem(productoId);
    }

    @Override
    public double calcularTotal() {
        return carrito.calcularTotal();
    }

    @Override
    public void verResumen() {
        if (carrito.estaVacio()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        System.out.println("--- Resumen del carrito ---");
        for (ItemCarrito item : carrito.getItems()) {
            System.out.println("  " + item.getProducto().getNombre()
                    + " x" + item.getCantidad()
                    + " = $" + item.calcularPrecioItem());
        }
        System.out.println("  TOTAL: $" + carrito.calcularTotal());
    }

    public Carrito getCarrito() {
        return carrito;
    }
}
