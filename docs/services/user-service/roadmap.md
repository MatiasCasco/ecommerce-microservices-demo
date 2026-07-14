# User Service Roadmap

## Objetivo

Este documento describe la evolución planificada de **User Service**.

El servicio constituye el proveedor de identidad (Identity Provider) del ecosistema y continuará evolucionando para ofrecer mayores capacidades de autenticación, autorización y seguridad.

---

# Estado Actual

## Gestión de Usuarios

- [x] Registro de usuarios.
- [x] Inicio de sesión.
- [x] Gestión de roles.
- [x] Almacenamiento seguro de credenciales.

---

## Seguridad

- [x] Spring Security.
- [x] JWT Authentication.
- [x] Password Hashing mediante BCrypt.
- [x] Stateless Authentication.
- [x] Role-Based Access Control (RBAC).

---

## API

- [x] Registro de usuarios.
- [x] Login.
- [x] OpenAPI / Swagger.

---

## Observabilidad

- [x] Logging estructurado.
- [x] TraceId distribuido.
- [x] Manejo centralizado de excepciones.

---

# Próxima Evolución

## Gestión de Tokens

- [ ] Refresh Tokens.
- [ ] Revocación de Tokens.
- [ ] Blacklist de Tokens.
- [ ] Expiración configurable.

### Objetivo

Mejorar la seguridad y la experiencia del usuario sin comprometer el modelo Stateless.

---

## Seguridad Avanzada

- [ ] Multi-Factor Authentication (MFA).
- [ ] Verificación de correo electrónico.
- [ ] Recuperación de contraseña.
- [ ] Cambio seguro de contraseña.
- [ ] Bloqueo temporal por múltiples intentos fallidos.

### Objetivo

Incrementar la protección de las cuentas de usuario.

---

## OAuth2

- [ ] Authorization Server.
- [ ] Client Credentials.
- [ ] OAuth2 Resource Server.
- [ ] Integración con aplicaciones externas.

### Objetivo

Permitir autenticación e integración con clientes externos utilizando estándares abiertos.

---

## Observabilidad

- [ ] OpenTelemetry.
- [ ] Distributed Tracing.
- [ ] Métricas de autenticación.
- [ ] Dashboards.
- [ ] Alertas de seguridad.

### Objetivo

Facilitar el monitoreo y análisis del comportamiento del servicio.

---

## Auditoría

- [ ] Historial de autenticaciones.
- [ ] Registro de inicios de sesión.
- [ ] Registro de cambios de contraseña.
- [ ] Registro de asignación de roles.

### Objetivo

Incrementar la trazabilidad y facilitar auditorías de seguridad.

---

# Integraciones Futuras

User Service continuará siendo el proveedor de identidad del ecosistema.

Será utilizado por:

- [x] Product Service
- [x] Order Service
- [ ] Notification Service
- [ ] Inventory Service
- [ ] Analytics Service

Todos los servicios utilizarán JWT para autenticar y autorizar solicitudes.

---

# Evolución Arquitectónica

El servicio continuará evolucionando siguiendo los principios definidos para el proyecto.

Próximas mejoras arquitectónicas:

- [ ] Refresh Token Strategy.
- [ ] OAuth2 Authorization Server.
- [ ] Rotación de claves JWT.
- [ ] Secret Management.
- [ ] Rate Limiting.
- [ ] Integración con Identity Providers externos.

---

# Estado de Madurez

| Área | Estado |
|-------|--------|
| Gestión de Usuarios | ✅ Maduro |
| Autenticación | ✅ Maduro |
| JWT | ✅ Maduro |
| Spring Security | ✅ Maduro |
| API REST | ✅ Maduro |
| Observabilidad | 🚧 En evolución |
| Seguridad Avanzada | 📅 Futuro |
| OAuth2 | 📅 Futuro |

---

# Visión

User Service evolucionará desde un servicio de autenticación basado en JWT hacia una plataforma completa de gestión de identidad y acceso (Identity and Access Management).

La evolución estará orientada a fortalecer la seguridad, mejorar la experiencia del usuario y soportar un ecosistema distribuido con múltiples aplicaciones y microservicios, manteniendo una arquitectura desacoplada, escalable y alineada con los estándares modernos de autenticación.