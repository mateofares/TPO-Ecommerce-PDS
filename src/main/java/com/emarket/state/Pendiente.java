package com.emarket.state;

import com.emarket.model.pedido.Pedido;

public class Pendiente implements EstadoPedido {

    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(EstadoPedidoNombre.PAGADO);
    }

    @Override
    public String getNombre() {
        return "Pendiente";
    }
}
