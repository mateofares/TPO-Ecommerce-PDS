package com.emarket.state;

import com.emarket.model.pedido.Pedido;

public class Enviado implements EstadoPedido {

    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(EstadoPedidoNombre.ENTREGADO);
    }

    @Override
    public String getNombre() {
        return "Enviado";
    }
}
