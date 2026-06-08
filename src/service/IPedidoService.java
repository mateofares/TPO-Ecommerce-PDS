package service;

import model.pedido.Pedido;
import model.usuario.Cliente;
import state.estadosPedidos.EstadoPedido;
import strategy.metodosPago.MetodoPago;

import java.util.List;

public interface IPedidoService {
    Pedido confirmarCompra(Cliente cliente, MetodoPago metodoPago);
    void actualizarEstado(long pedidoId, EstadoPedido nuevoEstado);
    List<Pedido> verTodosPedidos();
    Pedido buscarPorId(long id);
}
