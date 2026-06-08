package service;

import model.producto.Producto;

public interface ICarritoService {
    void agregarProducto(Producto producto, int cantidad);
    void modificarCantidad(long productoId, int nuevaCantidad);
    void eliminarItem(long productoId);
    double calcularTotal();
    void verResumen();
}
