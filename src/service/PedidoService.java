package service;

import model.carrito.ItemCarrito;
import model.observadoresNotificaciones.Email;
import model.observadoresNotificaciones.ObservadorPedido;
import model.pedido.Pedido;
import model.usuario.Cliente;
import model.usuario.Usuario;
import repository.IPedidoRepository;
import repository.IProductoRepository;
import repository.IUsuarioRepository;
import state.estadosPedidos.EstadoPedido;
import strategy.metodosPago.MetodoPago;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PedidoService implements IPedidoService {
    private final IPedidoRepository pedidoRepository;
    private final IProductoRepository productoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final AtomicLong nextId;

    public PedidoService(IPedidoRepository pedidoRepository, IProductoRepository productoRepository, IUsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.nextId = new AtomicLong(pedidoRepository.getMaxId() + 1);
    }

    private void suscribirObservadores(Pedido pedido, long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario != null) pedido.suscribir(new Email(usuario.getEmail()));
    }

    @Override
    public Pedido confirmarCompra(Cliente cliente, MetodoPago metodoPago) {
        if (cliente.getCarrito().estaVacio())
            throw new IllegalStateException("El carrito está vacío.");

        List<ItemCarrito> items = new ArrayList<>(cliente.getCarrito().getItems());
        List<ObservadorPedido> observadores = new ArrayList<>();

        Pedido pedido = new Pedido(nextId.getAndIncrement(), cliente.getId(), items, metodoPago, observadores);

        for (ItemCarrito item : items) {
            item.getProducto().reducirStock(item.getCantidad());
            productoRepository.updateStock(item.getProducto().getId(), item.getProducto().getStock());
        }

        pedido.confirmar();
        cliente.getCarrito().vaciar();
        cliente.agregarPedido(pedido);
        pedidoRepository.save(pedido);
        return pedido;
    }

    @Override
    public Pedido crearPedido(long usuarioId, List<ItemCarrito> items, MetodoPago metodoPago) {
        if (items == null || items.isEmpty())
            throw new IllegalStateException("El carrito está vacío.");

        for (ItemCarrito item : items) {
            if (!item.getProducto().hayStock(item.getCantidad()))
                throw new IllegalStateException("Stock insuficiente para: " + item.getProducto().getNombre());
        }

        Pedido pedido = new Pedido(nextId.getAndIncrement(), usuarioId, items, metodoPago, new ArrayList<>());
        suscribirObservadores(pedido, usuarioId);

        for (ItemCarrito item : items) {
            item.getProducto().reducirStock(item.getCantidad());
            productoRepository.updateStock(item.getProducto().getId(), item.getProducto().getStock());
        }

        pedido.confirmar();
        pedidoRepository.save(pedido);
        return pedido;
    }

    @Override
    public Pedido avanzarEstado(long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido == null) throw new IllegalArgumentException("Pedido #" + pedidoId + " no encontrado.");
        suscribirObservadores(pedido, pedido.getUsuarioId());
        pedido.avanzarEstado();
        pedidoRepository.updateEstado(pedidoId, pedido.getEstadoNombre());
        return pedido;
    }

    @Override
    public void actualizarEstado(long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido == null) throw new IllegalArgumentException("Pedido #" + pedidoId + " no encontrado.");
        suscribirObservadores(pedido, pedido.getUsuarioId());
        pedido.cambiarEstado(nuevoEstado);
        pedidoRepository.updateEstado(pedidoId, nuevoEstado.getNombre());
    }

    @Override
    public List<Pedido> verTodosPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    public List<Pedido> verPedidosDeUsuario(long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Pedido buscarPorId(long id) {
        return pedidoRepository.findById((int) id);
    }
}
