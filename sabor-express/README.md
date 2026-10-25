# Sabor Express (Java Swing + PostgreSQL)

## Ejecutar
1. Crea la base: `CREATE DATABASE bd_saborexpress;` (o restaura tu `sabor_express.sql` con pgAdmin).
2. Edita `src/main/resources/db.properties` (url, usuario, contraseña de PostgreSQL).
3. `mvn package` y luego `java -jar target/SaborExpress.jar`
   (o abre la carpeta en NetBeans/IntelliJ y ejecuta `sabor.app.Main`). Requiere JDK 17+.

Al iniciar, la app crea las tablas que falten y carga datos iniciales solo si están vacías.
Usuarios demo: mozo/mozo123 · cocinero/cocina123 · admin/admin123 (contraseñas guardadas con SHA-256).

## Estructura (MVC + patrones del informe)
- `model` entidades · `dao` consultas SQL (DAO) · `db.Conexion` (Singleton)
- `controller` lógica entre vistas y DAOs · `view` pantallas Swing
- `service.PedidoNotifier` (Observer) · `service.CajaFacade` (Facade)
