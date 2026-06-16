package service;

import model.pedido.Pedido;
import model.usuario.Cliente;
import state.estadosPedidos.EstadoPedido;
import strategy.metodosPago.MetodoPago;

import java.util.List;

public interface IPedidoService {
    Pedido confirmarCompra(Cliente cliente, MetodoPago metodoPago);
    Pedido crearPedido(long usuarioId, List<model.carrito.ItemCarrito> items, MetodoPago metodoPago);
    void actualizarEstado(long pedidoId, EstadoPedido nuevoEstado);
    Pedido avanzarEstado(long pedidoId);
    List<Pedido> verTodosPedidos();
    List<Pedido> verPedidosDeUsuario(long usuarioId);
    Pedido buscarPorId(long id);
}
