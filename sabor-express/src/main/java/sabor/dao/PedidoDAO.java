package sabor.dao;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sabor.db.Conexion;
import sabor.model.ItemPedido;
import sabor.model.Pedido;

public class PedidoDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    /** Guarda la comanda completa y marca la mesa como OCUPADA (todo en una transacción). */
    public int crear(int idMesa, int idUsuario, List<ItemPedido> items) throws SQLException {
        Connection c = cn();
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            try (PreparedStatement ps = c.prepareStatement("UPDATE mesas SET estado='OCUPADA' WHERE id_mesa=? AND estado='LIBRE'")) {
                ps.setInt(1, idMesa);
                if (ps.executeUpdate() == 0) throw new SQLException("La mesa ya no está libre. Actualiza el mapa de mesas.");
            }
            double total = items.stream().mapToDouble(ItemPedido::subtotal).sum();
            int id;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO pedidos(id_mesa,id_usuario,fecha_hora,estado,total) VALUES(?,?,?,'PENDIENTE',?) RETURNING id_pedido")) {
                ps.setInt(1, idMesa); ps.setInt(2, idUsuario);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(4, BigDecimal.valueOf(total));
                try (ResultSet r = ps.executeQuery()) { r.next(); id = r.getInt(1); }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO detalle_pedido(id_pedido,id_menu,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)")) {
                for (ItemPedido i : items) {
                    ps.setInt(1, id); ps.setInt(2, i.idMenu); ps.setInt(3, i.cantidad);
                    ps.setBigDecimal(4, BigDecimal.valueOf(i.precio));
                    ps.setBigDecimal(5, BigDecimal.valueOf(i.subtotal()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            return id;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    public List<Pedido> listarPorEstados(String... estados) throws SQLException {
        Connection c = cn();
        Map<Integer, Pedido> map = new java.util.LinkedHashMap<>();
        Array arr = c.createArrayOf("varchar", estados);
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT p.id_pedido,p.id_mesa,m.numero,p.estado,p.fecha_hora,p.total FROM pedidos p "
                + "JOIN mesas m ON m.id_mesa=p.id_mesa WHERE p.estado = ANY(?) ORDER BY p.fecha_hora")) {
            ps.setArray(1, arr);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    Pedido p = new Pedido(r.getInt(1), r.getInt(2), r.getInt(3), r.getString(4),
                            r.getTimestamp(5).toLocalDateTime(), r.getBigDecimal(6).doubleValue());
                    map.put(p.id, p);
                }
            }
        } finally { arr.free(); }
        if (map.isEmpty()) return new ArrayList<>();
        Array ids = c.createArrayOf("integer", map.keySet().toArray(new Integer[0]));
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT d.id_pedido,d.id_menu,mn.nombre,d.precio_unitario,d.cantidad FROM detalle_pedido d "
                + "JOIN menu mn ON mn.id_menu=d.id_menu WHERE d.id_pedido = ANY(?) ORDER BY d.id_detalle")) {
            ps.setArray(1, ids);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    map.get(r.getInt(1)).items.add(new ItemPedido(r.getInt(2), r.getString(3),
                            r.getBigDecimal(4).doubleValue(), r.getInt(5)));
                }
            }
        } finally { ids.free(); }
        return new ArrayList<>(map.values());
    }

    /** Pedido vigente (aún no cobrado) de una mesa, o null. */
    public Pedido activoDeMesa(int idMesa) throws SQLException {
        for (Pedido p : listarPorEstados("PENDIENTE", "EN_PREPARACION", "LISTO")) {
            if (p.idMesa == idMesa) return p;
        }
        return null;
    }

    /** Cambia el estado solo si el pedido sigue en "desde". Devuelve false si ya cambió. */
    public boolean cambiarEstado(int idPedido, String desde, String hacia) throws SQLException {
        Connection c = cn();
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int n;
            try (PreparedStatement ps = c.prepareStatement("UPDATE pedidos SET estado=? WHERE id_pedido=? AND estado=?")) {
                ps.setString(1, hacia); ps.setInt(2, idPedido); ps.setString(3, desde);
                n = ps.executeUpdate();
            }
            if (n > 0 && "LISTO".equals(hacia)) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE mesas SET estado='POR_PAGAR' WHERE id_mesa=(SELECT id_mesa FROM pedidos WHERE id_pedido=?)")) {
                    ps.setInt(1, idPedido);
                    ps.executeUpdate();
                }
            }
            c.commit();
            return n > 0;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    public void marcarCobrado(Connection c, int idPedido) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE pedidos SET estado='COBRADO' WHERE id_pedido=? AND estado='LISTO'")) {
            ps.setInt(1, idPedido);
            if (ps.executeUpdate() == 0) throw new SQLException("El pedido ya no está listo para cobro.");
        }
    }
}
