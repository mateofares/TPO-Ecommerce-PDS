package repository;

import model.carrito.ItemCarrito;
import model.pedido.Pedido;
import model.producto.Producto;
import strategy.metodosPago.MetodoPago;
import strategy.metodosPago.PagoPayPal;
import strategy.metodosPago.PagoTransferencia;
import strategy.metodosPago.TarjetaCredito;
import state.estadosPedidos.Enviado;
import state.estadosPedidos.Entregado;
import state.estadosPedidos.EstadoPedido;
import state.estadosPedidos.Pagado;
import state.estadosPedidos.Pendiente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PedidoRepository implements IPedidoRepository {
    private final ProductoRepository productoRepository;

    public PedidoRepository() {
        this.productoRepository = new ProductoRepository();
    }

    @Override
    public void save(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser nulo.");
        String pedidoSql = "INSERT INTO pedidos (id, fecha, estado, metodo_pago, total) VALUES (?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pedidoPs = conn.prepareStatement(pedidoSql);
             PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
            pedidoPs.setLong(1, pedido.getId());
            pedidoPs.setString(2, pedido.getFecha().toString());
            pedidoPs.setString(3, pedido.getEstadoNombre());
            pedidoPs.setString(4, pedido.getMetodoPago().getNombre());
            pedidoPs.setDouble(5, pedido.calcularTotal());
            pedidoPs.executeUpdate();

            for (ItemCarrito item : pedido.getItems()) {
                Producto producto = item.getProducto();
                itemPs.setLong(1, pedido.getId());
                itemPs.setLong(2, producto.getId());
                itemPs.setInt(3, item.getCantidad());
                itemPs.setDouble(4, producto.getPrecio());
                itemPs.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser nulo.");
        removeById((int) pedido.getId());
    }

    @Override
    public void removeById(int id) {
        String deleteItems = "DELETE FROM pedido_items WHERE pedido_id = ?";
        String deletePedido = "DELETE FROM pedidos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement itemPs = conn.prepareStatement(deleteItems);
             PreparedStatement pedidoPs = conn.prepareStatement(deletePedido)) {
            itemPs.setInt(1, id);
            itemPs.executeUpdate();
            pedidoPs.setInt(1, id);
            pedidoPs.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pedido findById(int id) {
        String sql = "SELECT id, fecha, estado, metodo_pago FROM pedidos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildPedidoFromResultSet(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Pedido> findAll() {
        String sql = "SELECT id, fecha, estado, metodo_pago FROM pedidos";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pedidos.add(buildPedidoFromResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pedidos;
    }

    private Pedido buildPedidoFromResultSet(ResultSet rs) throws Exception {
        long pedidoId = rs.getLong("id");
        String fecha = rs.getString("fecha");
        String estado = rs.getString("estado");
        String metodoPago = rs.getString("metodo_pago");

        List<model.carrito.ItemCarrito> items = loadItems(pedidoId);
        Pedido pedido = new Pedido(pedidoId, items, buildMetodoPago(metodoPago), new ArrayList<>());
        pedido.cambiarEstado(buildEstado(estado));
        return pedido;
    }

    private List<model.carrito.ItemCarrito> loadItems(long pedidoId) throws Exception {
        String sql = "SELECT producto_id, cantidad FROM pedido_items WHERE pedido_id = ?";
        List<model.carrito.ItemCarrito> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long productoId = rs.getLong("producto_id");
                    int cantidad = rs.getInt("cantidad");
                    Producto producto = productoRepository.findById(productoId);
                    if (producto != null) {
                        items.add(new model.carrito.ItemCarrito(producto, cantidad));
                    }
                }
            }
        }
        return items;
    }

    private MetodoPago buildMetodoPago(String metodoPago) {
        if (metodoPago == null) return new PagoTransferencia("", "");
        switch (metodoPago) {
            case "Tarjeta de Crédito":
                return new TarjetaCredito("0000000000000000", "000");
            case "PayPal":
                return new PagoPayPal("unknown", "token");
            case "Transferencia Bancaria":
                return new PagoTransferencia("", "");
            default:
                return new PagoTransferencia("", "");
        }
    }

    private EstadoPedido buildEstado(String estado) {
        if (estado == null) return new Pendiente();
        switch (estado) {
            case "Pagado": return new Pagado();
            case "Enviado": return new Enviado();
            case "Entregado": return new Entregado();
            default: return new Pendiente();
        }
    }
}
