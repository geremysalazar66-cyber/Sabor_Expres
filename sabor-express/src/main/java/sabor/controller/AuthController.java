package sabor.controller;

import java.sql.SQLException;
import sabor.dao.UsuarioDAO;
import sabor.model.Usuario;
import sabor.util.Fmt;

public class AuthController {
    private final UsuarioDAO dao = new UsuarioDAO();

    public Usuario login(String usuario, String clave, String rol) throws SQLException {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty())
            throw new IllegalArgumentException("Credenciales inválidas");
        Usuario u = dao.autenticar(usuario.trim(), Fmt.sha256(clave));
        if (u == null || !u.rol().equalsIgnoreCase(rol)) throw new IllegalArgumentException("Credenciales inválidas");
        return u;
    }
}
