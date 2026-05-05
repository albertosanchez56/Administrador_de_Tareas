-- Ejecutar como superusuario postgres (después de permitir trust en localhost y reiniciar el servicio).
-- Contraseña del rol postgres de desarrollo (cámbiala en producción):
ALTER USER postgres WITH PASSWORD 'taskboard_dev_2026';

CREATE DATABASE taskboard;
CREATE USER taskboard WITH PASSWORD 'taskboard';
ALTER DATABASE taskboard OWNER TO taskboard;

\c taskboard
GRANT ALL ON SCHEMA public TO taskboard;
