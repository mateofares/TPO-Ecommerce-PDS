package state.estadosPedidos;

import model.pedido.Pedido;

public class Entregado implements EstadoPedido {
    @Override
    public void avanzarEstado(Pedido pedido) {
        throw new IllegalStateException("El pedido ya fue entregado, no puede cambiar de estado");
    }

    @Override
    public String getNombre() {
        return "Entregado";
    }
}
