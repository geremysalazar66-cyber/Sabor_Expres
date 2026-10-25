package sabor.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Patrón Singleton: una sola conexión a PostgreSQL para toda la aplicación. */
public final class Conexion {
    private static Conexion instancia;
    private final String url, user, pass;
    private Connection cn;

    private Conexion() {
        Properties p = new Properties();
        try (InputStream in = Conexion.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (IOException ignored) { }
        File ext = new File("db.properties");
        if (ext.exists()) {
            try (InputStream in = new FileInputStream(ext)) { p.load(in); } catch (IOException ignored) { }
        }
        url = p.getProperty("db.url", "jdbc:postgresql://localhost:5432/bd_saborexpress");
        user = p.getProperty("db.user", "postgres");
        pass = p.getProperty("db.password", "postgres");
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) instancia = new Conexion();
        return instancia;
    }

    public synchronized Connection get() throws SQLException {
        if (cn == null || cn.isClosed() || !cn.isValid(2)) {
            cn = DriverManager.getConnection(url, user, pass);
        }
        return cn;
    }
}
