package com.emarket.service;

import com.emarket.dto.ConfirmarCompraRequest;
import com.emarket.dto.ItemCompraRequest;
import com.emarket.model.carrito.ItemCarrito;
import com.emarket.model.pedido.Pedido;
import com.emarket.model.producto.Producto;
import com.emarket.model.usuario.Cliente;
import com.emarket.repository.PedidoRepository;
import com.emarket.repository.ProductoRepository;
import com.emarket.state.EstadoPedidoNombre;
import com.emarket.strategy.MetodoPago;
import com.emarket.strategy.MetodoPagoFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         UsuarioService usuarioService,
                         NotificacionService notificacionService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    public Pedido confirmarCompra(ConfirmarCompraRequest req) {
        Cliente cliente = usuarioService.buscarCliente(req.clienteId());

        if (req.items() == null || req.items().isEmpty())
            throw new IllegalStateException("El carrito está vacío. Agregá productos antes de confirmar.");

        // buscamos cada producto en la BD
        List<ItemCarrito> itemsCarrito = new ArrayList<>();
        for (ItemCompraRequest itemReq : req.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemReq.productoId()));
            if (!producto.hayStock(itemReq.cantidad()))
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            itemsCarrito.add(new ItemCarrito(producto, itemReq.cantidad()));
        }

        double total = itemsCarrito.stream()
                .mapToDouble(i -> i.getProducto().getPrecio() * i.getCantidad())
                .sum();

        // elegimos cómo se va a pagar
        MetodoPago metodoPago = MetodoPagoFactory.crear(req);

        if (!metodoPago.pagar(total))
            throw new IllegalStateException("El pago fue rechazado por: " + metodoPago.getNombre());

        Pedido pedido = new Pedido(cliente, itemsCarrito,
                metodoPago.getTipo(), metodoPago.getIdentificador());

        // como el pago ya se procesó, pasamos a PAGADO
        pedido.avanzarEstado();

        // Descontar stock de cada producto
        itemsCarrito.forEach(item -> item.getProducto().reducirStock(item.getCantidad()));

        cliente.agregarPedido(pedido);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // mandamos email de confirmación automáticamente
        notificacionService.suscribir(pedidoGuardado.getId(), "EMAIL", cliente.getEmail());
        notificacionService.notificar(pedidoGuardado.getId(),
                "Pedido #" + pedidoGuardado.getId() + " confirmado. Estado: "
                        + pedidoGuardado.getEstadoNombreStr());

        return pedidoGuardado;
    }

    public Pedido avanzarEstado(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        // cargamos los observadores para que reciban la notificación
        notificacionService.getObservadores(pedidoId).forEach(pedido::suscribir);

        pedido.avanzarEstado();
        return pedidoRepository.save(pedido);
    }

    // el admin puede forzar un estado específico
    public Pedido cambiarEstado(Long pedidoId, EstadoPedidoNombre nuevoEstado) {
        Pedido pedido = buscarPorId(pedidoId);

        notificacionService.getObservadores(pedidoId).forEach(pedido::suscribir);
        pedido.cambiarEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }
}
