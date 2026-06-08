package model.usuario;

import model.carrito.Carrito;
import model.pedido.Pedido;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    private Carrito carrito;
    private List<Pedido> pedidos;

    public Cliente(long id, String nombre, String apellido, String email, String contrasenia) {
        super(id, nombre, apellido, email, contrasenia);
        this.carrito = new Carrito((int) id);
        this.pedidos = new ArrayList<>();
    }

    public void verCarrito() {
        List<model.carrito.ItemCarrito> items = carrito.getItems();
        if (items.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        System.out.println("=== Carrito de " + nombre + " ===");
        for (model.carrito.ItemCarrito item : items) {
            System.out.println("  " + item.getProducto().getNombre()
                    + " x" + item.getCantidad()
                    + " = $" + item.calcularPrecioItem());
        }
        System.out.println("  TOTAL: $" + carrito.calcularTotal());
    }

    public void verPedidos() {
        if (pedidos.isEmpty()) {
            System.out.println("No tenés pedidos registrados.");
            return;
        }
        System.out.println("=== Pedidos de " + nombre + " ===");
        for (Pedido p : pedidos) {
            System.out.println("  Pedido #" + p.getId()
                    + " | Fecha: " + p.getFecha()
                    + " | Estado: " + p.getEstadoNombre()
                    + " | Total: $" + p.calcularTotal());
        }
    }

    public void agregarPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public Carrito getCarrito() { return carrito; }
    public List<Pedido> getPedidos() { return pedidos; }
}
