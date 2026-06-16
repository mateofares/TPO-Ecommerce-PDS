package com.emarket.model.pedido;

import com.emarket.model.carrito.ItemCarrito;
import com.emarket.model.usuario.Cliente;
import com.emarket.observer.ObservadorPedido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.emarket.state.*;
import com.emarket.strategy.TipoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoNombre estadoNombre;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    // últimos 4 dígitos de la tarjeta, email de PayPal, banco, etc.
    private String identificadorPago;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    private List<PedidoItem> items = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // no se guardan en BD, se cargan en cada request
    @Transient
    private List<ObservadorPedido> observadores = new ArrayList<>();

    public Pedido(Cliente cliente, List<ItemCarrito> itemsCarrito,
                  TipoPago tipoPago, String identificadorPago) {
        this.cliente = cliente;
        this.fecha = LocalDate.now();
        this.estadoNombre = EstadoPedidoNombre.PENDIENTE;
        this.tipoPago = tipoPago;
        this.identificadorPago = identificadorPago;
        for (ItemCarrito ic : itemsCarrito) {
            this.items.add(new PedidoItem(ic.getProducto(), ic.getCantidad()));
        }
    }

    public void avanzarEstado() {
        resolverEstado().avanzarEstado(this);
    }

    public void cambiarEstado(EstadoPedidoNombre nuevoEstado) {
        this.estadoNombre = nuevoEstado;
        notificarObservadores("Pedido #" + id + " cambió a estado: " + resolverEstado().getNombre());
    }

    // devuelve el objeto de estado que corresponde al enum guardado
    private EstadoPedido resolverEstado() {
        return switch (estadoNombre) {
            case PENDIENTE  -> new Pendiente();
            case PAGADO     -> new Pagado();
            case ENVIADO    -> new Enviado();
            case ENTREGADO  -> new Entregado();
        };
    }

    public String getEstadoNombreStr() {
        return resolverEstado().getNombre();
    }

    public void suscribir(ObservadorPedido observador) {
        if (observadores == null) observadores = new ArrayList<>();
        observadores.add(observador);
    }

    public void notificarObservadores(String mensaje) {
        if (observadores != null) {
            for (ObservadorPedido o : observadores) {
                o.actualizar(mensaje);
            }
        }
    }

    @JsonProperty("total")
    public double calcularTotal() {
        return items.stream().mapToDouble(PedidoItem::calcularSubtotal).sum();
    }
}
