

### Paso 1: Asegúrate de que PostgreSQL está activo

La aplicación necesita comunicarse con la base de datos, por lo tanto, el servicio de PostgreSQL debe estar corriendo en tu PC. 

---

### Paso 2: Crea la base de datos vacía

Abre tu herramienta de PostgreSQL y crea una base de datos con este nombre exacto:

CREATE DATABASE bd_saborexpress;

```


### Paso 3: Revisa que tus credenciales estén en el archivo

Abre el archivo `db.properties` (que ya habías ubicado antes en `src/main/resources/db.properties`) y asegúrate de que tenga bien puesto tu usuario y contraseña de Postgres:

db.url=jdbc:postgresql://localhost:5432/bd_saborexpress
db.user= *tu_usuario*
db.password= *tu_clave*


Guarda el archivo si cambiaste algo.

---

### Paso 4: La forma más fácil de compilar y correr 


1. Asegúrate de tener abierto el archivo **`Main.java`** en la pantalla principal de NetBeans 


2. Haz **clic derecho** en cualquier parte vacía del código de ese archivo `Main.java`.


3. Selecciona la opción **Run File**

DEBES CONVERTIR LA CLASE "main" en un MAIN CLASS PARA QUE FUNCIONE


###  Datos para iniciar sesión cuando abra la app:

Mozo: mozo / mozo123
Cocinero: cocinero / cocina123
Admin: admin / admin123
* **Admin:** `admin` / `admin123`

Prueba presionar `Shift + F6` sobre el `Main.java` y cuéntame si la ventana de SaborExpress por fin abrió en tu pantalla.
