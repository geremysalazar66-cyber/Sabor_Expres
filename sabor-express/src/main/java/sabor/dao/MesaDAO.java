package sabor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sabor.db.Conexion;
import sabor.model.Mesa;

public class MesaDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    public List<Mesa> listar() throws SQLException {
        List<Mesa> l = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement("SELECT id_mesa,numero,asientos,estado FROM mesas ORDER BY numero");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) l.add(new Mesa(r.getInt(1), r.getInt(2), r.getInt(3), r.getString(4)));
        }
        return l;
    }

    public void actualizarEstado(Connection c, int idMesa, String estado) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE mesas SET estado=? WHERE id_mesa=?")) {
            ps.setString(1, estado); ps.setInt(2, idMesa);
            ps.executeUpdate();
        }
    }

    public int contarActivas() throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT COUNT(*) FROM mesas WHERE estado<>'LIBRE'");
             ResultSet r = ps.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }
}
