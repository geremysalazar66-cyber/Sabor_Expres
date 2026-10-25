package sabor.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import sabor.util.Fmt;

/**
 * Crea las tablas si no existen (mismo script del informe, punto 6.1.4) y carga datos iniciales
 * solo cuando las tablas están vacías.
 */
public final class DatabaseInit {
    private DatabaseInit() {}

    public static void iniciar() throws SQLException {
        Connection c = Conexion.getInstancia().get();
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS roles (id_rol SERIAL PRIMARY KEY, nombre_rol VARCHAR(30) UNIQUE NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS usuarios (id_usuario SERIAL PRIMARY KEY, nombre VARCHAR(100) NOT NULL, "
                    + "usuario VARCHAR(50) UNIQUE NOT NULL, contrasena VARCHAR(255) NOT NULL, id_rol INT NOT NULL, "
                    + "CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol))");
            s.execute("CREATE TABLE IF NOT EXISTS mesas (id_mesa SERIAL PRIMARY KEY, numero INT UNIQUE NOT NULL, estado VARCHAR(20) NOT NULL)");
            // Única columna nueva respecto al informe: cantidad de asientos (el prototipo la muestra en cada mesa)
            s.execute("ALTER TABLE mesas ADD COLUMN IF NOT EXISTS asientos INT NOT NULL DEFAULT 4");
            s.execute("CREATE TABLE IF NOT EXISTS menu (id_menu SERIAL PRIMARY KEY, nombre VARCHAR(100) NOT NULL, "
                    + "categoria VARCHAR(50) NOT NULL, precio NUMERIC(10,2) NOT NULL, estado VARCHAR(20) NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS pedidos (id_pedido SERIAL PRIMARY KEY, id_mesa INT NOT NULL, id_usuario INT NOT NULL, "
                    + "fecha_hora TIMESTAMP NOT NULL, estado VARCHAR(30) NOT NULL, total NUMERIC(10,2) NOT NULL, "
                    + "CONSTRAINT fk_pedidos_mesa FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa), "
                    + "CONSTRAINT fk_pedidos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
            s.execute("CREATE TABLE IF NOT EXISTS detalle_pedido (id_detalle SERIAL PRIMARY KEY, id_pedido INT NOT NULL, id_menu INT NOT NULL, "
                    + "cantidad INT NOT NULL, precio_unitario NUMERIC(10,2) NOT NULL, subtotal NUMERIC(10,2) NOT NULL, "
                    + "CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido), "
                    + "CONSTRAINT fk_detalle_menu FOREIGN KEY (id_menu) REFERENCES menu(id_menu))");
            s.execute("CREATE TABLE IF NOT EXISTS pagos (id_pago SERIAL PRIMARY KEY, id_pedido INT UNIQUE NOT NULL, fecha_hora TIMESTAMP NOT NULL, "
                    + "metodo_pago VARCHAR(50) NOT NULL, monto_total NUMERIC(10,2) NOT NULL, "
                    + "CONSTRAINT fk_pagos_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido))");
            s.execute("CREATE TABLE IF NOT EXISTS reportes (id_reporte SERIAL PRIMARY KEY, id_usuario_creador INT NOT NULL, "
                    + "fecha_inicio DATE NOT NULL, fecha_fin DATE NOT NULL, cantidad_pedidos INT NOT NULL, total_ventas NUMERIC(10,2) NOT NULL, "
                    + "CONSTRAINT fk_reportes_usuario FOREIGN KEY (id_usuario_creador) REFERENCES usuarios(id_usuario))");
        }
        sembrar(c);
    }

    private static boolean vacia(Connection c, String tabla) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            r.next();
            return r.getInt(1) == 0;
        }
    }

    private static int idRol(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id_rol FROM roles WHERE lower(nombre_rol)=lower(?)")) {
            ps.setString(1, nombre);
            try (ResultSet r = ps.executeQuery()) { if (r.next()) return r.getInt(1); }
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO roles(nombre_rol) VALUES(?) RETURNING id_rol")) {
            ps.setString(1, nombre);
            try (ResultSet r = ps.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    private static void sembrar(Connection c) throws SQLException {
        int admin = idRol(c, "Administrador"), cocinero = idRol(c, "Cocinero"), mozo = idRol(c, "Mozo");

        if (vacia(c, "usuarios")) {
            String sql = "INSERT INTO usuarios(nombre,usuario,contrasena,id_rol) VALUES(?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                Object[][] u = {
                    {"Administrador del Sistema", "admin", "admin123", admin},
                    {"Carlos Ruiz", "mozo", "mozo123", mozo},
                    {"María Quispe", "cocinero", "cocina123", cocinero}};
                for (Object[] x : u) {
                    ps.setString(1, (String) x[0]); ps.setString(2, (String) x[1]);
                    ps.setString(3, Fmt.sha256((String) x[2])); ps.setInt(4, (Integer) x[3]);
                    ps.executeUpdate();
                }
            }
        }
        if (vacia(c, "mesas")) {
            int[] asientos = {4, 2, 6, 4, 2, 4, 6, 4, 2, 4, 6, 2};
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO mesas(numero,estado,asientos) VALUES(?,?,?)")) {
                for (int i = 0; i < asientos.length; i++) {
                    ps.setInt(1, i + 1); ps.setString(2, "LIBRE"); ps.setInt(3, asientos[i]);
                    ps.executeUpdate();
                }
            }
        }
        if (vacia(c, "menu")) {
            Object[][] m = {
                {"Causa Limeña", "Entradas", 18.0}, {"Ceviche Clásico", "Entradas", 36.0},
                {"Anticuchos", "Entradas", 22.0}, {"Papa a la Huancaína", "Entradas", 16.0},
                {"Lomo Saltado", "Principales", 42.0}, {"Ají de Gallina", "Principales", 38.0},
                {"Seco de Res", "Principales", 44.0}, {"Pollo a la Brasa", "Principales", 52.0},
                {"Inca Kola", "Bebidas", 8.0}, {"Agua Mineral", "Bebidas", 6.0},
                {"Cerveza Cristal", "Bebidas", 14.0}, {"Chicha Morada", "Bebidas", 10.0},
                {"Arroz con Leche", "Postres", 16.0}, {"Suspiro a la Limeña", "Postres", 14.0},
                {"Mazamorra Morada", "Postres", 12.0}};
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO menu(nombre,categoria,precio,estado) VALUES(?,?,?,'DISPONIBLE')")) {
                for (Object[] x : m) {
                    ps.setString(1, (String) x[0]); ps.setString(2, (String) x[1]);
                    ps.setBigDecimal(3, java.math.BigDecimal.valueOf((Double) x[2]));
                    ps.executeUpdate();
                }
            }
        }
    }
}
