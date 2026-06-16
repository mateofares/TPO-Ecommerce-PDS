package com.emarket.state;

import com.emarket.model.pedido.Pedido;

// estado final, no se puede avanzar más
public class Entregado implements EstadoPedido {

    @Override
    public void avanzarEstado(Pedido pedido) {
        throw new IllegalStateException("El pedido ya fue entregado y no puede cambiar de estado.");
    }

    @Override
    public String getNombre() {
        return "Entregado";
    }
}
