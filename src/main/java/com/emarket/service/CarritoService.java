package com.emarket.service;

import com.emarket.model.carrito.Carrito;
import com.emarket.model.carrito.ItemCarrito;
import com.emarket.model.producto.Producto;
import com.emarket.model.usuario.Cliente;
import com.emarket.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CarritoService {

    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final CarritoRepository carritoRepository;

    public CarritoService(UsuarioService usuarioService,
                          ProductoService productoService,
                          CarritoRepository carritoRepository) {
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.carritoRepository = carritoRepository;
    }

    public Carrito agregarProducto(Long clienteId, Long productoId, int cantidad) {
        Cliente cliente = usuarioService.buscarCliente(clienteId);
        Producto producto = productoService.buscarPorId(productoId);
        cliente.getCarrito().agregar(producto, cantidad);
        return carritoRepository.save(cliente.getCarrito());
    }

    public Carrito modificarCantidad(Long clienteId, Long productoId, int nuevaCantidad) {
        Cliente cliente = usuarioService.buscarCliente(clienteId);
        cliente.getCarrito().modificarCantidad(productoId, nuevaCantidad);
        return carritoRepository.save(cliente.getCarrito());
    }

    public Carrito eliminarItem(Long clienteId, Long productoId) {
        Cliente cliente = usuarioService.buscarCliente(clienteId);
        cliente.getCarrito().eliminarItem(productoId);
        return carritoRepository.save(cliente.getCarrito());
    }

    @Transactional(readOnly = true)
    public Carrito obtenerCarrito(Long clienteId) {
        return usuarioService.buscarCliente(clienteId).getCarrito();
    }

    @Transactional(readOnly = true)
    public List<ItemCarrito> obtenerItems(Long clienteId) {
        return obtenerCarrito(clienteId).getItems();
    }

    @Transactional(readOnly = true)
    public double calcularTotal(Long clienteId) {
        return obtenerCarrito(clienteId).calcularTotal();
    }
}
