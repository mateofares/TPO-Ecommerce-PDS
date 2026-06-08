package state.estadosPedidos;

import model.pedido.Pedido;

public interface EstadoPedido {
    void avanzarEstado(Pedido pedido);
    String getNombre();
}
