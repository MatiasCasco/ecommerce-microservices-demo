# Authentication Flow

## Objetivo

Este documento describe los principales flujos funcionales implementados por **User Service**.

User Service es responsable de autenticar usuarios, emitir tokens JWT y proporcionar un mecanismo seguro de acceso al ecosistema.

---

# Flujo de Registro

```text
             Client
                │
                ▼
      POST /auth/register
                │
                ▼
        Auth Controller
                │
                ▼
         Auth Service
                │
                ▼
      Validar Request
                │
                ▼
 Verificar Usuario Existente
                │
                ▼
      Encriptar Password
          (BCrypt)
                │
                ▼
         Crear Usuario
                │
                ▼
        Persistir Usuario
                │
                ▼
        Respuesta Exitosa
```

---

## Descripción

Durante el registro de un usuario se realizan las siguientes acciones:

- Validar la solicitud.
- Verificar que el correo electrónico no exista.
- Encriptar la contraseña utilizando BCrypt.
- Crear la entidad User.
- Persistir la información.
- Retornar una respuesta exitosa.

---

# Flujo de Autenticación

```text
             Client
                │
                ▼
       POST /auth/login
                │
                ▼
        Auth Controller
                │
                ▼
         Auth Service
                │
                ▼
     Validar Credenciales
                │
                ▼
 Spring Security Authentication
                │
                ▼
       AuthenticationManager
                │
                ▼
      UserDetailsService
                │
                ▼
     Usuario Autenticado
                │
                ▼
         Generar JWT
                │
                ▼
        AuthResponse
```

---

## Descripción

Durante la autenticación se realizan las siguientes acciones:

- Validar las credenciales del usuario.
- Verificar la contraseña utilizando BCrypt.
- Autenticar mediante Spring Security.
- Generar un JWT.
- Retornar el token al cliente.

---

# Flujo de Autorización

Una vez autenticado, el cliente utiliza el JWT para acceder a los demás microservicios.

```text
             Client
                │
                ▼
 Authorization: Bearer JWT
                │
                ▼
      Protected Service
                │
                ▼
      JWT Authentication Filter
                │
                ▼
       Validar Token
                │
                ▼
   Extraer Usuario y Roles
                │
                ▼
 Spring Security Context
                │
                ▼
   Acceso Autorizado
```

User Service no participa nuevamente durante este proceso.

Los demás microservicios validan el JWT de manera independiente.

---

# Gestión de Contraseñas

```text
Password

      │

      ▼

BCrypt Encoder

      │

      ▼

Hash Seguro

      │

      ▼

Base de Datos
```

Las contraseñas nunca son almacenadas en texto plano.

---

# Emisión del JWT

Después de una autenticación exitosa:

```text
Usuario Autenticado

        │

        ▼

JwtService

        │

        ▼

Construcción del Token

        │

        ▼

JWT Firmado

        │

        ▼

Cliente
```

El JWT representa la identidad autenticada del usuario y será utilizado para acceder a los recursos protegidos.

---

# Principios del Flujo

Todos los casos de uso siguen los mismos principios:

- Validar antes de autenticar.
- Nunca almacenar contraseñas en texto plano.
- Utilizar BCrypt para proteger credenciales.
- Emitir JWT únicamente después de una autenticación exitosa.
- Mantener una arquitectura Stateless.
- Delegar la autorización a Spring Security utilizando los Roles contenidos en el JWT.

---

# Evolución

En futuras iteraciones el flujo evolucionará incorporando:

- Refresh Tokens.
- OAuth2.
- Client Credentials.
- Multi-Factor Authentication (MFA).
- Password Recovery.
- Account Verification.
- Revocación de Tokens.
- Auditoría de autenticaciones.