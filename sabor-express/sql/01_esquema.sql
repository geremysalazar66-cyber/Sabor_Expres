-- Opcional: la aplicación crea todo esto sola al iniciar. Ejecuta solo si quieres preparar la BD a mano.
-- CREATE DATABASE bd_saborexpress;   (ejecutar aparte, conectado a "postgres")
ALTER TABLE IF EXISTS mesas ADD COLUMN IF NOT EXISTS asientos INT NOT NULL DEFAULT 4;
