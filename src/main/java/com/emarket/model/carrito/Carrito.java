package com.emarket.model.carrito;

import com.emarket.model.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "carrito_id")
    private List<ItemCarrito> items = new ArrayList<>();

    public void agregar(Producto producto, int cantidad) {
        if (!producto.hayStock(cantidad))
            throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());

        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(producto.getId())) {
                item.modificarCantidad(item.getCantidad() + cantidad);
                return;
            }
        }
        items.add(new ItemCarrito(producto, cantidad));
    }

    public void modificarCantidad(Long productoId, int nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(productoId)) {
                if (nuevaCantidad <= 0) {
                    items.remove(item);
                } else {
                    item.modificarCantidad(nuevaCantidad);
                }
                return;
            }
        }
    }

    public void eliminarItem(Long productoId) {
        items.removeIf(i -> i.getProducto().getId().equals(productoId));
    }

    public double calcularTotal() {
        return items.stream().mapToDouble(ItemCarrito::calcularPrecioItem).sum();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void vaciar() {
        items.clear();
    }
}
