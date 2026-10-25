package sabor.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import sabor.dao.MenuDAO;
import sabor.dao.MesaDAO;
import sabor.dao.ReporteDAO;
import sabor.dao.UsuarioDAO;
import sabor.model.Plato;
import sabor.model.ResumenDia;
import sabor.model.TopPlato;
import sabor.model.Usuario;
import sabor.util.Fmt;

public class AdminController {
    private final ReporteDAO reportes = new ReporteDAO();
    private final MesaDAO mesas = new MesaDAO();
    private final MenuDAO menu = new MenuDAO();
    private final UsuarioDAO usuarios = new UsuarioDAO();

    // ----- panel -----
    public ResumenDia resumen(LocalDate d) throws SQLException { return reportes.resumen(d); }
    public Map<Integer, Double> ventasPorHora(LocalDate d) throws SQLException { return reportes.ventasPorHora(d); }
    public List<TopPlato> masPedidos(LocalDate d) throws SQLException { return reportes.topPlatos(d, 5); }
    public int mesasActivas() throws SQLException { return mesas.contarActivas(); }

    // ----- menú -----
    public List<Plato> platos() throws SQLException { return menu.listar(); }

    public void guardarPlato(Integer id, String nombre, String categoria, String precioTxt, String estado) throws SQLException {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Ingresa el nombre del plato.");
        double precio;
        try { precio = Double.parseDouble(precioTxt.trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("El precio debe ser un valor numérico."); }
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        if (id == null) menu.crear(nombre.trim(), categoria, precio, estado);
        else menu.actualizar(id, nombre.trim(), categoria, precio, estado);
    }

    // ----- usuarios -----
    public List<Usuario> usuarios() throws SQLException { return usuarios.listar(); }
    public Map<String, Integer> roles() throws SQLException { return usuarios.roles(); }

    public void crearUsuario(String nombre, String usuario, String clave, int idRol) throws SQLException {
        if (nombre.isBlank() || usuario.isBlank() || clave.isEmpty()) throw new IllegalArgumentException("Completa nombre, usuario y contraseña.");
        if (usuarios.existeUsuario(usuario.trim()))
            throw new IllegalArgumentException("El nombre de usuario ya existe. Ingresa uno diferente.");
        usuarios.crear(nombre.trim(), usuario.trim(), Fmt.sha256(clave), idRol);
    }

    public void cambiarRol(int idUsuario, int idRol) throws SQLException { usuarios.cambiarRol(idUsuario, idRol); }

    public void eliminarUsuario(int idUsuario, int idActual) throws SQLException {
        if (idUsuario == idActual) throw new IllegalArgumentException("No puedes eliminar tu propio usuario.");
        try { usuarios.eliminar(idUsuario); }
        catch (SQLException e) { throw new IllegalStateException("No se puede eliminar: el usuario ya tiene pedidos o reportes registrados."); }
    }
}
