# User Service Domain

## Objetivo

Este documento describe el dominio funcional de **User Service**.

Su propósito es documentar las reglas de negocio, responsabilidades y decisiones de diseño relacionadas con la gestión de identidades y autenticación dentro del ecosistema.

No pretende describir detalles de implementación, sino representar el comportamiento esperado del dominio.

---

# ¿Qué representa User Service?

User Service es responsable de administrar la identidad de los usuarios del sistema.

Representa el dominio de autenticación y autorización del ecosistema, proporcionando un mecanismo seguro para registrar usuarios, validar credenciales y emitir tokens JWT utilizados por los demás microservicios.

---

# Identity Provider

User Service actúa como el proveedor de identidad (Identity Provider) del ecosistema.

Es el único responsable de:

- registrar usuarios
- validar credenciales
- emitir tokens JWT
- administrar roles

Los demás microservicios confían en la información contenida en el JWT para autorizar el acceso a sus recursos protegidos.

---

# User

User representa una identidad dentro del sistema.

Cada usuario posee la información necesaria para autenticarse y acceder a las funcionalidades permitidas por su rol.

Información principal:

- nombre
- correo electrónico
- contraseña cifrada
- rol

---

# Credenciales

Las credenciales representan el mecanismo de autenticación del usuario.

Actualmente están compuestas por:

- email
- password

Las contraseñas nunca son almacenadas en texto plano.

Siempre son protegidas mediante BCrypt.

---

# Roles

El dominio implementa un modelo de autorización basado en roles (RBAC).

Actualmente existen dos roles.

## ROLE_ADMIN

Permite administrar los recursos del sistema.

Ejemplos:

- gestionar productos
- gestionar categorías
- operaciones administrativas

---

## ROLE_USER

Representa un usuario autenticado con permisos limitados.

Puede consumir únicamente las funcionalidades permitidas para clientes del sistema.

---

# JWT

Una autenticación exitosa genera un JSON Web Token (JWT).

El token representa la identidad autenticada del usuario y contiene la información necesaria para que los demás microservicios autoricen el acceso sin consultar nuevamente a User Service.

User Service es el único responsable de emitir estos tokens.

---

# Responsabilidades del Dominio

El dominio conoce sus propias reglas.

Entre ellas:

- registrar usuarios
- autenticar credenciales
- cifrar contraseñas
- emitir JWT
- administrar roles

Los Services únicamente coordinan los casos de uso.

---

# Seguridad

La autenticación se basa en los siguientes principios.

## Stateless Authentication

Cada solicitud es independiente.

El servidor no mantiene sesiones.

Toda la información necesaria viaja dentro del JWT.

---

## Password Hashing

Las contraseñas nunca se almacenan en texto plano.

Siempre son protegidas utilizando BCrypt.

---

## Role-Based Access Control

El acceso a los recursos protegidos depende del rol del usuario autenticado.

La autorización se realiza utilizando la información contenida en el JWT.

---

# Principios del Dominio

Durante el diseño del servicio se adoptaron los siguientes principios.

## Identity Provider

User Service representa la única fuente oficial de identidades del ecosistema.

---

## Stateless Authentication

No existen sesiones almacenadas en el servidor.

Cada petición es completamente independiente.

---

## Seguridad

Toda autenticación requiere la validación de credenciales antes de emitir un JWT.

---

## Bajo Acoplamiento

Los demás microservicios no necesitan consultar User Service para autenticar cada solicitud.

Únicamente validan el JWT recibido.

---

## Evolución del Dominio

El modelo fue diseñado para evolucionar gradualmente.

Próximas etapas:

- Refresh Tokens
- OAuth2
- Client Credentials
- Multi-Factor Authentication (MFA)
- Password Recovery
- Account Verification
- Auditoría de autenticación
- OpenTelemetry

Cada nueva funcionalidad deberá respetar los principios establecidos en este documento.