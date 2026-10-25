package sabor.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sabor.db.Conexion;
import sabor.model.PagoReg;

public class PagoDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    private static final String BASE = "SELECT p.id_pedido,m.numero,g.fecha_hora,g.monto_total,g.metodo_pago FROM pagos g "
            + "JOIN pedidos p ON p.id_pedido=g.id_pedido JOIN mesas m ON m.id_mesa=p.id_mesa ";

    public void registrar(Connection c, int idPedido, double monto, String metodo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO pagos(id_pedido,fecha_hora,metodo_pago,monto_total) VALUES(?,?,?,?)")) {
            ps.setInt(1, idPedido); ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, metodo); ps.setBigDecimal(4, BigDecimal.valueOf(monto));
            ps.executeUpdate();
        }
    }

    private List<PagoReg> leer(PreparedStatement ps) throws SQLException {
        List<PagoReg> l = new ArrayList<>();
        try (ResultSet r = ps.executeQuery()) {
            while (r.next()) l.add(new PagoReg(r.getInt(1), r.getInt(2), r.getTimestamp(3).toLocalDateTime(),
                    r.getBigDecimal(4).doubleValue(), r.getString(5)));
        }
        return l;
    }

    public List<PagoReg> recientes(int limite) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(BASE + "ORDER BY g.fecha_hora DESC LIMIT ?")) {
            ps.setInt(1, limite);
            return leer(ps);
        }
    }

    public List<PagoReg> delDia(LocalDate d) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                BASE + "WHERE g.fecha_hora>=? AND g.fecha_hora<? ORDER BY g.fecha_hora")) {
            ps.setTimestamp(1, Timestamp.valueOf(d.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(d.plusDays(1).atStartOfDay()));
            return leer(ps);
        }
    }
}
