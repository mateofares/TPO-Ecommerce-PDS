package com.emarket.state;

import com.emarket.model.pedido.Pedido;

public class Pagado implements EstadoPedido {

    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(EstadoPedidoNombre.ENVIADO);
    }

    @Override
    public String getNombre() {
        return "Pagado";
    }
}
