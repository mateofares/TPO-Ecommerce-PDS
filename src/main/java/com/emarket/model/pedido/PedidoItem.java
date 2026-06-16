package com.emarket.model.pedido;

import com.emarket.model.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido_items")
@Getter
@Setter
@NoArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;

    // guardamos el precio al momento de la compra para que no cambie si el producto se actualiza
    private double precioUnitario;

    public PedidoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public double calcularSubtotal() {
        return precioUnitario * cantidad;
    }
}
