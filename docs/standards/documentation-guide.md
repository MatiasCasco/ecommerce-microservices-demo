# User Service - Design Decisions

## Objetivo

Este documento registra las principales decisiones de diseño adoptadas durante el desarrollo de **User Service**.

Su propósito es explicar el razonamiento detrás del modelo de autenticación y autorización del sistema, facilitando el mantenimiento, la evolución del servicio y la incorporación de nuevos desarrolladores.

Las decisiones aquí documentadas son específicas de User Service. Las decisiones que afectan a toda la arquitectura del ecosistema se documentan mediante Architecture Decision Records (ADR).

---

# Decisiones del Dominio

## User Service como Identity Provider

### Problema

Era necesario definir qué servicio sería responsable de autenticar usuarios y administrar identidades.

### Decisión

User Service será el único proveedor de identidad (Identity Provider) del ecosistema.

Toda autenticación deberá realizarse exclusivamente mediante este servicio.

### Justificación

- Centraliza la autenticación.
- Evita duplicar lógica de seguridad.
- Simplifica la administración de usuarios.
- Permite que los demás servicios permanezcan desacoplados.

---

## Gestión Centralizada de Usuarios

### Problema

Las credenciales no deben distribuirse entre múltiples microservicios.

### Decisión

Toda la información relacionada con usuarios será administrada únicamente por User Service.

### Justificación

- Reduce riesgos de seguridad.
- Centraliza la administración de credenciales.
- Facilita futuras integraciones con proveedores externos de identidad.

---

# Decisiones de Autenticación

## JWT como mecanismo de autenticación

### Problema

Era necesario autenticar usuarios entre múltiples microservicios sin mantener sesiones compartidas.

### Decisión

El sistema utilizará JSON Web Tokens (JWT).

### Justificación

- Arquitectura Stateless.
- Bajo acoplamiento.
- Escalabilidad.
- Integración sencilla entre microservicios.

---

## Stateless Authentication

### Problema

Mantener sesiones compartidas complica la escalabilidad del sistema.

### Decisión

User Service no almacenará sesiones de usuario.

Cada solicitud deberá contener un JWT válido.

### Justificación

- Facilita el escalado horizontal.
- Reduce consumo de memoria.
- Simplifica el despliegue distribuido.

---

## Spring Security

### Problema

Era necesario contar con un mecanismo robusto para autenticar y autorizar usuarios.

### Decisión

La autenticación se implementará utilizando Spring Security.

### Justificación

- Framework ampliamente adoptado.
- Integración nativa con Spring Boot.
- Alta flexibilidad.
- Buen soporte para JWT.

---

# Decisiones de Seguridad

## BCrypt para almacenamiento de contraseñas

### Problema

Las contraseñas nunca deben almacenarse en texto plano.

### Decisión

Todas las contraseñas serán protegidas utilizando BCrypt.

### Justificación

- Algoritmo diseñado para hashing de contraseñas.
- Incrementa la resistencia frente a ataques de fuerza bruta.
- Recomendado por Spring Security.

---

## Role-Based Access Control (RBAC)

### Problema

Los usuarios poseen distintos niveles de acceso dentro del sistema.

### Decisión

La autorización se basará en Roles.

Actualmente:

- ROLE_ADMIN
- ROLE_USER

### Justificación

- Modelo simple.
- Fácil mantenimiento.
- Escalable para futuras funcionalidades.

---

# Decisiones de Integración

## Los demás servicios confían en el JWT

### Problema

Los microservicios no deberían consultar User Service en cada solicitud.

### Decisión

Los consumidores validarán localmente el JWT recibido.

### Justificación

- Reduce llamadas entre servicios.
- Disminuye la latencia.
- Mantiene bajo acoplamiento.
- Incrementa la disponibilidad.

---

## User Service no participa en Event-Driven

### Problema

No todas las operaciones justifican comunicación mediante eventos.

### Decisión

Actualmente User Service no publica ni consume eventos de dominio.

### Justificación

Su responsabilidad principal es la autenticación.

La comunicación con los consumidores se realiza mediante JWT.

---

# Decisiones de Observabilidad

## Logging Estructurado

### Decisión

Las operaciones relevantes generan logs estructurados utilizando CommerceLog.

### Justificación

Permite:

- auditoría
- monitoreo
- depuración
- trazabilidad

---

## Propagación de TraceId

### Decisión

Cada solicitud incorpora un TraceId para facilitar el seguimiento entre microservicios.

### Justificación

Mejora la observabilidad del ecosistema y facilita el diagnóstico de problemas.

---

# Decisiones Futuras

## Refresh Tokens

Actualmente los JWT poseen una expiración fija.

En futuras versiones se incorporarán Refresh Tokens para mejorar la experiencia del usuario sin comprometer la seguridad.

---

## OAuth2

User Service evolucionará hacia un Authorization Server compatible con OAuth2.

Esto permitirá integrar aplicaciones externas y soportar distintos tipos de clientes.

---

## Multi-Factor Authentication (MFA)

Se incorporará autenticación multifactor para incrementar la seguridad de las cuentas.

---

## Identity Federation

El servicio podrá integrarse con proveedores externos de identidad.

Ejemplos:

- Google
- GitHub
- Microsoft Entra ID
- Keycloak
- Auth0

---

# Principios Generales

Todas las futuras decisiones del servicio deberán respetar los siguientes principios:

- User Service es el único proveedor de identidad del ecosistema.
- Las credenciales nunca se almacenan en texto plano.
- Toda autenticación pasa por User Service.
- La autorización se realiza mediante JWT y Roles.
- Los microservicios permanecen desacoplados.
- La seguridad tiene prioridad sobre la comodidad.
- La arquitectura evoluciona de manera incremental.