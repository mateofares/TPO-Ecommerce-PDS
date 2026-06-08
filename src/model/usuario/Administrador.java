package model.usuario;

import model.pedido.Pedido;
import state.estadosPedidos.EstadoPedido;

import java.util.List;

public class Administrador extends Usuario {
    private int legajo;

    public Administrador(long id, String nombre, String apellido, String email, String contrasenia, int legajo) {
        super(id, nombre, apellido, email, contrasenia);
        this.legajo = legajo;
    }

    public void actualizarEstado(Pedido pedido, EstadoPedido nuevoEstado) {
        pedido.cambiarEstado(nuevoEstado);
        System.out.println("[Admin " + nombre + "] Pedido #" + pedido.getId()
                + " actualizado a: " + nuevoEstado.getNombre());
    }

    public void verTodosPedidos(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("=== Todos los pedidos ===");
        for (Pedido p : pedidos) {
            System.out.println("  Pedido #" + p.getId()
                    + " | Fecha: " + p.getFecha()
                    + " | Estado: " + p.getEstadoNombre()
                    + " | Total: $" + p.calcularTotal()
                    + " | Pago: " + p.getMetodoPago().getNombre());
        }
    }

    public int getLegajo() { return legajo; }
}
