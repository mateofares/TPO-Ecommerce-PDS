package state.estadosPedidos;

import model.pedido.Pedido;

public class Enviado implements EstadoPedido{
    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(new Entregado());
    }

    @Override
    public String getNombre() {
        return "Enviado";
    }
}
