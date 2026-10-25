package sabor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sabor.db.Conexion;
import sabor.model.Usuario;

public class UsuarioDAO {
    private Connection cn() throws SQLException { return Conexion.getInstancia().get(); }

    private static final String BASE = "SELECT u.id_usuario,u.nombre,u.usuario,r.nombre_rol FROM usuarios u "
            + "JOIN roles r ON r.id_rol=u.id_rol ";

    public Usuario autenticar(String usuario, String hash) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(BASE + "WHERE u.usuario=? AND u.contrasena=?")) {
            ps.setString(1, usuario); ps.setString(2, hash);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? new Usuario(r.getInt(1), r.getString(2), r.getString(3), r.getString(4)) : null;
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> l = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(BASE + "ORDER BY u.id_usuario");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) l.add(new Usuario(r.getInt(1), r.getString(2), r.getString(3), r.getString(4)));
        }
        return l;
    }

    public Map<String, Integer> roles() throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        try (PreparedStatement ps = cn().prepareStatement("SELECT id_rol,nombre_rol FROM roles ORDER BY id_rol");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) m.put(r.getString(2), r.getInt(1));
        }
        return m;
    }

    public boolean existeUsuario(String usuario) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT 1 FROM usuarios WHERE lower(usuario)=lower(?)")) {
            ps.setString(1, usuario);
            try (ResultSet r = ps.executeQuery()) { return r.next(); }
        }
    }

    public void crear(String nombre, String usuario, String hash, int idRol) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement(
                "INSERT INTO usuarios(nombre,usuario,contrasena,id_rol) VALUES(?,?,?,?)")) {
            ps.setString(1, nombre); ps.setString(2, usuario); ps.setString(3, hash); ps.setInt(4, idRol);
            ps.executeUpdate();
        }
    }

    public void cambiarRol(int idUsuario, int idRol) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("UPDATE usuarios SET id_rol=? WHERE id_usuario=?")) {
            ps.setInt(1, idRol); ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void cambiarContrasena(int idUsuario, String hash) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("UPDATE usuarios SET contrasena=? WHERE id_usuario=?")) {
            ps.setString(1, hash); ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM usuarios WHERE id_usuario=?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }
}
