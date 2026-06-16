package model.pedido;

import model.carrito.ItemCarrito;
import model.observadoresNotificaciones.ObservadorPedido;
import state.estadosPedidos.EstadoPedido;
import state.estadosPedidos.Pendiente;
import strategy.metodosPago.MetodoPago;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private long id;
    private long usuarioId;
    private LocalDate fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private List<ItemCarrito> items;
    private List<ObservadorPedido> observadores;

    public Pedido(long id, long usuarioId, List<ItemCarrito> items, MetodoPago metodoPago, List<ObservadorPedido> observadores) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = LocalDate.now();
        this.estado = new Pendiente();
        this.items = new ArrayList<>(items);
        this.metodoPago = metodoPago;
        this.observadores = new ArrayList<>(observadores);
    }

    public Pedido(long id, List<ItemCarrito> items, MetodoPago metodoPago, List<ObservadorPedido> observadores) {
        this(id, 0L, items, metodoPago, observadores);
    }

    public void confirmar() {
        double total = calcularTotal();
        boolean pagado = metodoPago.pagar(total);
        if (pagado) {
            avanzarEstado();
        } else {
            throw new IllegalStateException("El pago fue rechazado por el método: " + metodoPago.getNombre());
        }
    }

    public void avanzarEstado() {
        estado.avanzarEstado(this);
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
        notificarObservadores("Pedido #" + id + " cambio a estado: " + estado.getNombre());
    }

    public void notificarObservadores(String msg) {
        for (ObservadorPedido o : observadores) {
            o.actualizar(msg);
        }
    }

    public void suscribir(ObservadorPedido observador) {
        observadores.add(observador);
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.calcularPrecioItem();
        }
        return total;
    }

    public long getId() { return id; }
    public long getUsuarioId() { return usuarioId; }
    public LocalDate getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public String getEstadoNombre() { return estado.getNombre(); }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public List<ItemCarrito> getItems() { return items; }
}
