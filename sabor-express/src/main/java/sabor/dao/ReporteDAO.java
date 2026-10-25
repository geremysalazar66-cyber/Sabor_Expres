package sabor.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import sabor.db.Conexion;
import sabor.model.ResumenDia;
import sabor.model.TopPlato;

public class ReporteDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    private static void rango(PreparedStatement ps, LocalDate d) throws SQLException {
        ps.setTimestamp(1, Timestamp.valueOf(d.atStartOfDay()));
        ps.setTimestamp(2, Timestamp.valueOf(d.plusDays(1).atStartOfDay()));
    }

    public ResumenDia resumen(LocalDate d) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto_total),0), COUNT(*), COALESCE(MAX(monto_total),0), "
                + "COALESCE(SUM(monto_total) FILTER (WHERE metodo_pago='EFECTIVO'),0), "
                + "COALESCE(SUM(monto_total) FILTER (WHERE metodo_pago='TARJETA'),0), "
                + "COALESCE(SUM(monto_total) FILTER (WHERE metodo_pago='YAPE_PLIN'),0) "
                + "FROM pagos WHERE fecha_hora>=? AND fecha_hora<?";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            rango(ps, d);
            try (ResultSet r = ps.executeQuery()) {
                r.next();
                return new ResumenDia(r.getDouble(1), r.getInt(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6));
            }
        }
    }

    public Map<Integer, Double> ventasPorHora(LocalDate d) throws SQLException {
        Map<Integer, Double> m = new TreeMap<>();
        String sql = "SELECT EXTRACT(HOUR FROM fecha_hora)::int, SUM(monto_total) FROM pagos "
                + "WHERE fecha_hora>=? AND fecha_hora<? GROUP BY 1 ORDER BY 1";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            rango(ps, d);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) m.put(r.getInt(1), r.getDouble(2)); }
        }
        return m;
    }

    public List<TopPlato> topPlatos(LocalDate d, int limite) throws SQLException {
        List<TopPlato> l = new ArrayList<>();
        String sql = "SELECT mn.nombre, SUM(dp.cantidad) q FROM detalle_pedido dp "
                + "JOIN pedidos p ON p.id_pedido=dp.id_pedido JOIN menu mn ON mn.id_menu=dp.id_menu "
                + "WHERE p.fecha_hora>=? AND p.fecha_hora<? GROUP BY mn.nombre ORDER BY q DESC, mn.nombre LIMIT ?";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            rango(ps, d);
            ps.setInt(3, limite);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) l.add(new TopPlato(r.getString(1), r.getInt(2))); }
        }
        return l;
    }

    public void guardar(int idUsuario, LocalDate d, int cantidadPedidos, double totalVentas) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "INSERT INTO reportes(id_usuario_creador,fecha_inicio,fecha_fin,cantidad_pedidos,total_ventas) VALUES(?,?,?,?,?)")) {
            ps.setInt(1, idUsuario); ps.setDate(2, Date.valueOf(d)); ps.setDate(3, Date.valueOf(d));
            ps.setInt(4, cantidadPedidos); ps.setBigDecimal(5, BigDecimal.valueOf(totalVentas));
            ps.executeUpdate();
        }
    }
}
