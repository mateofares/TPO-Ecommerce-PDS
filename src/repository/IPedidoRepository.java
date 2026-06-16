package repository;

import model.pedido.Pedido;

import java.util.List;

public interface IPedidoRepository {
    void save(Pedido pedido);
    void remove(Pedido pedido);
    void removeById(int id);
    Pedido findById(int id);
    List<Pedido> findAll();
    List<Pedido> findByUsuarioId(long usuarioId);
    void updateEstado(long pedidoId, String estado);
    long getMaxId();
}
