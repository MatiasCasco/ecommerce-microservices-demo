# User Service

## Descripción

User Service es responsable de gestionar la identidad y autenticación de los usuarios dentro del ecosistema **Ecommerce Microservices Demo**.

Actúa como el proveedor de identidad (Identity Provider) del sistema, administrando el registro de usuarios, la autenticación mediante credenciales y la emisión de tokens JWT utilizados por los demás microservicios.

Su responsabilidad principal es garantizar que únicamente usuarios autenticados puedan acceder a los recursos protegidos del sistema.

---

# Responsabilidades

Actualmente el servicio es responsable de:

- Registrar nuevos usuarios.
- Autenticar usuarios.
- Generar tokens JWT.
- Gestionar credenciales.
- Administrar roles de usuario.
- Validar credenciales mediante Spring Security.
- Proteger el acceso al ecosistema.

---

# Arquitectura

User Service representa el punto de entrada para la autenticación dentro del ecosistema.

```text
               Client
                  │
                  ▼
        POST /auth/login
                  │
                  ▼
            User Service
                  │
                  ▼
      Validate Credentials
                  │
                  ▼
            Generate JWT
                  │
                  ▼
             JWT Token
                  │
                  ▼
        Protected Services

      ┌───────────┼─────────────┐
      ▼           ▼             ▼
 Product      Order       Future Services
 Service      Service
```

Todos los microservicios confían en el JWT emitido por User Service para autorizar el acceso a sus recursos protegidos.

---

# Principios de Diseño

Este servicio fue diseñado siguiendo los siguientes principios:

- Domain-Driven Design (DDD)
- Stateless Authentication
- JWT Authentication
- Spring Security
- Role-Based Access Control (RBAC)
- Bajo acoplamiento entre microservicios

---

# Funcionalidades

## Implementadas

- Registro de usuarios.
- Inicio de sesión.
- Generación de JWT.
- Password Hashing mediante BCrypt.
- Autenticación con Spring Security.
- Autorización basada en Roles.
- Logging estructurado.
- Propagación de TraceId.
- Manejo centralizado de excepciones.

## Futuro

- Refresh Tokens.
- OAuth2.
- Client Credentials.
- Multi-Factor Authentication (MFA).
- Account Verification.
- Password Recovery.
- Auditoría de autenticación.

---

# Documentación

Para mayor información consultar:

- domain.md
- authentication-flow.md
- api.md
- roadmap.md
- future.md
- decisions.md

---

# Tecnologías

- Java 21
- Spring Boot 3
- Spring Security
- JWT
- BCrypt
- PostgreSQL
- Log4j2
- OpenAPI / Swagger
- Maven

---

# Estado

✅ Servicio funcional.

Actualmente User Service administra la autenticación del ecosistema mediante JWT y proporciona un mecanismo seguro para el registro e inicio de sesión de usuarios.