package model.carrito;

import model.producto.Producto;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public double calcularPrecioItem() {
        return producto.getPrecio() * cantidad;
    }

    public void modificarCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
}
