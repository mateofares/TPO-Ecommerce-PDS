package service;

import model.carrito.ItemCarrito;
import model.observadoresNotificaciones.ObservadorPedido;
import model.pedido.Pedido;
import model.usuario.Cliente;
import repository.IPedidoRepository;
import state.estadosPedidos.EstadoPedido;
import strategy.metodosPago.MetodoPago;

import java.util.ArrayList;
import java.util.List;

public class PedidoService implements IPedidoService {
    private IPedidoRepository pedidoRepository;
    private long nextId = 1;

    public PedidoService(IPedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Pedido confirmarCompra(Cliente cliente, MetodoPago metodoPago) {
        if (cliente.getCarrito().estaVacio())
            throw new IllegalStateException("El carrito está vacío. Agregá productos antes de confirmar.");

        List<ItemCarrito> items = new ArrayList<>(cliente.getCarrito().getItems());

        // El cliente como observador (por email si tiene suscripciones configuradas)
        List<ObservadorPedido> observadores = new ArrayList<>();

        Pedido pedido = new Pedido(nextId++, items, metodoPago, observadores);

        // Descuenta stock de cada producto
        for (ItemCarrito item : items) {
            item.getProducto().reducirStock(item.getCantidad());
        }

        pedido.confirmar();
        cliente.getCarrito().vaciar();
        cliente.agregarPedido(pedido);
        pedidoRepository.save(pedido);

        System.out.println("Pedido #" + pedido.getId() + " confirmado. Estado: " + pedido.getEstadoNombre());
        return pedido;
    }

    @Override
    public void actualizarEstado(long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido == null) throw new IllegalArgumentException("Pedido #" + pedidoId + " no encontrado.");
        pedido.cambiarEstado(nuevoEstado);
    }

    @Override
    public List<Pedido> verTodosPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido buscarPorId(long id) {
        return pedidoRepository.findById((int) id);
    }
}
