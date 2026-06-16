package com.emarket.state;

import com.emarket.model.pedido.Pedido;

public interface EstadoPedido {
    void avanzarEstado(Pedido pedido);
    String getNombre();
}
