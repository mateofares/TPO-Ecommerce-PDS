package state.estadosPedidos;

import model.pedido.Pedido;

public class Pagado implements EstadoPedido{
    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(new Enviado());
    }

    @Override
    public String getNombre() {
        return "Pagado";
    }
}
