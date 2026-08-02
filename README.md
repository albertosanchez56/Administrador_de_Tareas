# Taskboard

Aplicación web de gestión de tableros y tareas estilo Kanban. Permite organizar el trabajo en columnas, colaborar con miembros del equipo y mantener el estado de cada tarea sincronizado entre clientes.

## Características

- Autenticación con registro, login y JWT
- Listado y creación de tableros
- Vista Kanban con columnas **Por hacer / En curso / Hecho**
- Creación, edición y eliminación de tareas
- Drag & drop entre columnas (cambio de estado y posición)
- Fecha de entrega (asignar y quitar)
- Asignación de tareas a miembros del tablero
- Gestión de miembros: listar, invitar por email y quitar (solo el propietario)
- Interfaz responsive (escritorio y móvil)

## Stack

| Capa | Tecnología |
|------|------------|
| Frontend | Angular 20, Angular CDK, RxJS |
| Backend | Java 17, Spring Boot 4, Spring Security, Spring Data JPA |
| Base de datos | PostgreSQL |
| Migraciones | Flyway |
| Auth | JWT (JJWT) |

## Estructura del repositorio

```
AdministradorTareasEstiloTrello/
├── taskboard-api/     # API REST (Spring Boot)
└── taskboard-web/     # Cliente web (Angular)
```

## Requisitos

- Java 17+
- Node.js 20+ (recomendado) y npm
- PostgreSQL 14+
- Maven Wrapper incluido en `taskboard-api` (`mvnw` / `mvnw.cmd`)

## Configuración

### Base de datos

Crea una base PostgreSQL. Valores por defecto del API:

| Variable | Valor por defecto |
|----------|-------------------|
| URL | `jdbc:postgresql://localhost:5432/taskboard` |
| Usuario | `taskboard` |
| Contraseña | `taskboard` |

Puedes sobrescribirlos con:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### JWT

Define un secreto para firmar los tokens:

```bash
# Windows (PowerShell)
$env:APP_SECURITY_JWT_SECRET = "cambia-este-secreto-por-uno-largo-y-aleatorio"

# Linux / macOS
export APP_SECURITY_JWT_SECRET="cambia-este-secreto-por-uno-largo-y-aleatorio"
```

El access token caduca a los **15 minutos** (`app.security.jwt.access-token-ttl-minutes`).

## Arranque en local

### 1. API

```bash
cd taskboard-api
./mvnw spring-boot:run
# Windows:
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8081`.

Health check: `GET /api/health`

### 2. Frontend

```bash
cd taskboard-web
npm install
npm start
```

`npm start` levanta Angular con proxy hacia `http://localhost:8081` (`proxy.conf.json`), de modo que las peticiones a `/api/**` se reenvían al backend.

Abre la app en `http://localhost:4200`.

## Uso rápido

1. Registra un usuario (o inicia sesión si ya existe).
2. Crea un tablero desde `/boards`.
3. Entra al tablero para gestionar tareas en el Kanban.
4. Usa el panel **Miembros** para invitar a otros usuarios registrados (por email) o quitar miembros.

## API (resumen)

Base: `http://localhost:8081/api`

### Auth

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/auth/register` | Registro |
| `POST` | `/auth/login` | Login (devuelve JWT) |
| `GET` | `/auth/me` | Usuario autenticado |

Las rutas protegidas requieren cabecera:

```http
Authorization: Bearer <accessToken>
```

### Tableros

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/boards` | Tableros del usuario |
| `POST` | `/boards` | Crear tablero |
| `GET` | `/boards/{boardId}/members` | Listar miembros |
| `POST` | `/boards/{boardId}/members` | Invitar por email (`{ "email": "..." }`) |
| `DELETE` | `/boards/{boardId}/members/{userId}` | Quitar miembro |

### Tareas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/boards/{boardId}/tasks` | Listar tareas |
| `POST` | `/boards/{boardId}/tasks` | Crear tarea |
| `PATCH` | `/boards/{boardId}/tasks/{taskId}` | Actualizar (título, descripción, status, posición, dueAt, asignado, etc.) |
| `DELETE` | `/boards/{boardId}/tasks/{taskId}` | Eliminar tarea |

## Scripts útiles

**Backend**

```bash
cd taskboard-api
./mvnw test
./mvnw package
```

**Frontend**

```bash
cd taskboard-web
npm start          # desarrollo + proxy
npm run build      # build de producción
npm test           # tests unitarios
```

## Notas

- El esquema de base de datos lo aplica **Flyway** al arrancar (`spring.jpa.hibernate.ddl-auto=none`).
- El frontend no llama al API en otro origen en desarrollo: usa el proxy de Angular.
- Solo el **propietario** del tablero puede invitar o expulsar miembros; el propietario no puede eliminarse a sí mismo del board.

## Licencia

Uso privado / según lo que indiques en el repositorio.
