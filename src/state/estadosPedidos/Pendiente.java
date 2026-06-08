package state.estadosPedidos;

import model.pedido.Pedido;

public class Pendiente implements EstadoPedido{

    @Override
    public void avanzarEstado(Pedido pedido) {
        pedido.cambiarEstado(new Pagado());
    }

    @Override
    public String getNombre() {
        return "Pendiente";
    }
}
