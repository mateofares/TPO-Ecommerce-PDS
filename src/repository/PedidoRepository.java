package repository;

import model.pedido.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidoRepository implements IPedidoRepository {
    private List<Pedido> pedidos;

    public PedidoRepository() {
        pedidos = new ArrayList<>();
    }

    @Override
    public void save(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser nulo.");
        pedidos.add(pedido);
    }

    @Override
    public void remove(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser nulo.");
        pedidos.remove(pedido);
    }

    @Override
    public void removeById(int id) {
        pedidos.removeIf(p -> p.getId() == id);
    }

    @Override
    public Pedido findById(int id) {
        for (Pedido p : pedidos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public List<Pedido> findAll() {
        return new ArrayList<>(pedidos);
    }
}
