# User Service API

## Objetivo

Este documento describe la API pública expuesta por **User Service**.

Su propósito es explicar el comportamiento funcional de cada endpoint, las reglas de negocio aplicadas y las operaciones disponibles relacionadas con la autenticación y gestión de usuarios.

La especificación técnica completa (OpenAPI/Swagger) representa la fuente oficial para los contratos HTTP.

---

# Base Path

```
/auth
```

---

# Recursos

User Service expone operaciones relacionadas con:

- Registro de usuarios.
- Autenticación.
- Emisión de JWT.

---

# Endpoints

## Registrar Usuario

### POST /auth/register

Registra un nuevo usuario dentro del sistema.

### Request

Información requerida:

- firstName
- lastName
- email
- password

### Reglas de negocio

- El correo electrónico debe ser único.
- La contraseña nunca se almacena en texto plano.
- La contraseña se protege utilizando BCrypt.
- El usuario recibe un rol por defecto según las reglas del negocio.

### Respuesta

Se registra el usuario y se devuelve una confirmación de la operación.

---

## Autenticación

### POST /auth/login

Autentica un usuario utilizando sus credenciales.

### Request

- email
- password

### Flujo

Durante la autenticación se realizan las siguientes acciones:

- Validar la solicitud.
- Buscar el usuario.
- Verificar la contraseña utilizando BCrypt.
- Autenticar mediante Spring Security.
- Generar un JWT.
- Retornar el token al cliente.

### Respuesta

```
JWT
```

El token deberá enviarse posteriormente utilizando el encabezado:

```
Authorization: Bearer <token>
```

---

# Seguridad

La API utiliza autenticación basada en JWT.

Después del login, User Service no mantiene sesiones.

Todas las solicitudes posteriores utilizan el JWT emitido durante la autenticación.

---

# Roles

Actualmente el sistema soporta los siguientes roles.

| Rol | Descripción |
|------|-------------|
| ROLE_ADMIN | Administración completa del sistema. |
| ROLE_USER | Usuario autenticado con permisos limitados. |

Los permisos específicos son definidos por cada microservicio consumidor.

---

# Validaciones

Durante las operaciones se validan:

- formato del correo electrónico
- unicidad del usuario
- credenciales válidas
- contraseña correcta

Si alguna validación falla, la operación es rechazada.

---

# Respuestas

Las respuestas siguen un formato consistente para operaciones exitosas y errores.

Los errores de negocio son manejados mediante excepciones centralizadas utilizando los componentes compartidos de `common-lib`.

---

# Integración

User Service actúa como proveedor de identidad del ecosistema.

Los demás microservicios utilizan el JWT emitido por User Service para autorizar el acceso a sus recursos protegidos.

Actualmente User Service:

- no publica eventos
- no consume eventos

La integración se realiza mediante tokens JWT.

---

# Observaciones

La documentación funcional de la API se complementa con:

- domain.md
- authentication-flow.md
- roadmap.md
- future.md
- decisions.md

La documentación técnica de contratos HTTP se encuentra disponible mediante OpenAPI/Swagger.