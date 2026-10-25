package sabor.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sabor.db.Conexion;
import sabor.model.Plato;

public class MenuDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    public List<Plato> listar() throws SQLException {
        List<Plato> l = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(
                "SELECT id_menu,nombre,categoria,precio,estado FROM menu ORDER BY id_menu");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) l.add(new Plato(r.getInt(1), r.getString(2), r.getString(3),
                    r.getBigDecimal(4).doubleValue(), r.getString(5)));
        }
        return l;
    }

    public void crear(String nombre, String categoria, double precio, String estado) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "INSERT INTO menu(nombre,categoria,precio,estado) VALUES(?,?,?,?)")) {
            ps.setString(1, nombre); ps.setString(2, categoria);
            ps.setBigDecimal(3, BigDecimal.valueOf(precio)); ps.setString(4, estado);
            ps.executeUpdate();
        }
    }

    public void actualizar(int id, String nombre, String categoria, double precio, String estado) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "UPDATE menu SET nombre=?,categoria=?,precio=?,estado=? WHERE id_menu=?")) {
            ps.setString(1, nombre); ps.setString(2, categoria);
            ps.setBigDecimal(3, BigDecimal.valueOf(precio)); ps.setString(4, estado); ps.setInt(5, id);
            ps.executeUpdate();
        }
    }
}
