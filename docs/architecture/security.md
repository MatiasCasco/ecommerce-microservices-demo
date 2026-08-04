# Seguridad

## Propósito

Este documento describe la estrategia de seguridad implementada en la plataforma para proteger los recursos expuestos por los microservicios.

El objetivo es garantizar que únicamente usuarios autenticados y autorizados puedan acceder a las operaciones correspondientes, manteniendo la independencia de cada servicio y un modelo de autenticación consistente en toda la plataforma.

Este documento complementa:

- overview.md
- flow.md
- distributed-tracing.md
- logging.md

---

# El Problema

Los microservicios exponen múltiples endpoints que permiten realizar operaciones críticas.

Por ejemplo:

- crear productos
- modificar precios
- actualizar stock
- crear órdenes
- consultar información

Sin un mecanismo de seguridad cualquier cliente podría ejecutar estas operaciones.

La plataforma necesita responder preguntas como:

- ¿Quién realiza la solicitud?
- ¿Está autenticado?
- ¿Tiene permisos suficientes?
- ¿Puede acceder a este recurso?

---

# Estrategia de Seguridad

La plataforma implementa un esquema de autenticación basado en JSON Web Tokens (JWT).

El proceso general es el siguiente.

```
Cliente

↓

Login

↓

User Service

↓

JWT

↓

Cliente

↓

Solicitud HTTP

↓

API Gateway

↓

Microservicio
```

Cada solicitud protegida incorpora el token generado durante la autenticación.

---

# Autenticación

La autenticación consiste en verificar la identidad del usuario.

Cuando un usuario inicia sesión:

1. Envía sus credenciales.
2. User Service valida la información.
3. Se genera un JWT.
4. El token es devuelto al cliente.

En las solicitudes posteriores el cliente envía ese mismo token.

---

# Autorización

Una vez autenticado el usuario, la plataforma verifica si posee permisos para ejecutar la operación solicitada.

La autorización se realiza mediante roles.

Ejemplo:

```
ROLE_ADMIN

ROLE_USER
```

Cada endpoint define qué roles pueden acceder.

Por ejemplo:

```
POST /products

↓

ROLE_ADMIN
```

Mientras que operaciones de consulta pueden estar disponibles para otros perfiles autorizados.

---

# JSON Web Token (JWT)

El JWT representa la identidad autenticada del usuario.

El token contiene información necesaria para validar la solicitud sin consultar nuevamente la base de datos.

Entre los datos incluidos pueden encontrarse:

- identificador del usuario
- nombre de usuario
- roles
- fecha de expiración

El servidor valida el token en cada solicitud antes de permitir el acceso al recurso.

---

# Flujo de una Solicitud Protegida

```
Cliente

↓

Authorization: Bearer <JWT>

↓

JwtAuthenticationFilter

↓

Validación del Token

↓

Spring Security

↓

Controlador

↓

Servicio
```

Si el token es válido, la solicitud continúa su procesamiento.

---

# Manejo de Errores

La plataforma diferencia distintos escenarios de seguridad.

## Usuario no autenticado

Si la solicitud no contiene un token válido, el acceso es rechazado.

Respuesta:

```
401 Unauthorized
```

---

## Usuario sin permisos

Si el usuario está autenticado pero intenta acceder a un recurso no permitido para su rol, la solicitud es rechazada.

Respuesta:

```
403 Forbidden
```

---

# Implementación en la Plataforma

La estrategia de seguridad se implementa mediante componentes compartidos y configuraciones específicas de cada microservicio.

```
common-lib

├── JwtUtil

Product Service

├── SecurityConfig
├── JwtAuthenticationFilter
├── CustomAuthenticationEntryPoint
└── CustomAccessDeniedHandler
```

---

## JwtUtil

`JwtUtil` centraliza la creación y validación de los tokens JWT.

Su responsabilidad consiste en evitar duplicar la lógica de manejo de tokens entre los distintos microservicios.

Permite:

- generar tokens
- validar firmas
- obtener información del usuario
- verificar expiración

---

## JwtAuthenticationFilter

Intercepta todas las solicitudes HTTP protegidas.

Durante el procesamiento:

1. Obtiene el token del encabezado `Authorization`.
2. Valida su contenido.
3. Extrae la identidad del usuario.
4. Registra la autenticación en el contexto de Spring Security.

Si el token no es válido, la solicitud no continúa.

---

## SecurityConfig

Centraliza la configuración de Spring Security.

Define aspectos como:

- endpoints públicos
- endpoints protegidos
- políticas de autenticación
- autorización por roles
- filtros de seguridad

Cada microservicio mantiene su propia configuración adaptada a sus necesidades.

---

## CustomAuthenticationEntryPoint

Gestiona las solicitudes que no poseen autenticación válida.

Su responsabilidad consiste en generar respuestas HTTP consistentes cuando el usuario intenta acceder sin un token válido.

Respuesta típica:

```
401 Unauthorized
```

---

## CustomAccessDeniedHandler

Gestiona los casos donde el usuario está autenticado pero no posee permisos suficientes.

Respuesta típica:

```
403 Forbidden
```

Esto permite diferenciar claramente entre problemas de autenticación y autorización.

---

# Relación con Otros Componentes

La seguridad trabaja en conjunto con otros mecanismos de la plataforma.

```
Solicitud

↓

TraceIdFilter

↓

JwtAuthenticationFilter

↓

Spring Security

↓

Lógica de Negocio

↓

CommerceLog
```

De esta manera:

- todas las solicitudes autenticadas poseen Trace ID
- los eventos de seguridad quedan registrados mediante logs estructurados
- los errores de autenticación pueden correlacionarse con el resto de la operación

---

# Buenas Prácticas

La plataforma sigue las siguientes recomendaciones.

- No almacenar contraseñas en texto plano.
- Utilizar contraseñas cifradas.
- Validar el token en cada solicitud protegida.
- Aplicar autorización basada en roles.
- Centralizar la lógica de autenticación.
- Registrar eventos de seguridad relevantes.
- Evitar exponer información sensible en mensajes de error.

---

# Aprendizajes Durante el Desarrollo

Durante la implementación surgieron varios aprendizajes importantes.

## Autenticación y autorización son conceptos diferentes

Validar la identidad de un usuario no implica que tenga permisos para ejecutar cualquier operación.

Ambos procesos deben mantenerse separados.

---

## La seguridad debe ser transversal

Cada microservicio protege sus propios recursos.

No depende de otros servicios para validar el acceso a sus endpoints.

---

## Los errores también forman parte de la experiencia

Diferenciar correctamente entre respuestas `401` y `403` facilita el diagnóstico tanto para desarrolladores como para consumidores de la API.

---

# Evolución Futura

La arquitectura permite incorporar mecanismos adicionales de seguridad.

Por ejemplo:

- Refresh Tokens
- OAuth 2.0
- OpenID Connect
- Rotación de claves
- Gestión centralizada de identidades
- API Keys para integraciones externas
- Rate Limiting

Estas mejoras pueden integrarse sin modificar la arquitectura general de autenticación y autorización.

---

# Conclusión

La seguridad constituye un pilar fundamental de la plataforma.

Mediante la combinación de JWT, Spring Security, autenticación, autorización basada en roles y manejo consistente de errores, cada microservicio protege sus recursos de forma independiente, manteniendo un modelo uniforme de acceso y facilitando la evolución futura de la arquitectura.

---

# Arquitectura de una Solicitud Protegida

El siguiente diagrama resume el recorrido que realiza una solicitud autenticada dentro de la plataforma.

```
                    Cliente
                       │
                       │
        Authorization: Bearer <JWT>
                       │
                       ▼
              API Gateway (Opcional)
                       │
                       ▼
               TraceIdFilter
                       │
         Genera o recupera Trace ID
                       │
                       ▼
         JwtAuthenticationFilter
                       │
            Extrae y valida JWT
                       │
                       ▼
          SecurityContextHolder
                       │
     Usuario autenticado y roles
                       │
                       ▼
             Spring Security
                       │
          ¿Tiene autorización?
              │              │
             Sí              No
              │              │
              ▼              ▼
      Controlador REST   403 Forbidden
              │
              ▼
       Lógica de Negocio
              │
              ▼
         CommerceLog
              │
              ▼
     Persistencia / RabbitMQ
```

Este flujo muestra cómo una solicitud es procesada por los distintos componentes de seguridad antes de llegar a la lógica de negocio.

Cada componente posee una responsabilidad específica:

- **TraceIdFilter**: genera o reutiliza el identificador de trazabilidad de la solicitud.
- **JwtAuthenticationFilter**: valida el token JWT y autentica al usuario.
- **SecurityContextHolder**: almacena la información del usuario autenticado durante toda la ejecución.
- **Spring Security**: verifica que el usuario tenga permisos para acceder al recurso solicitado.
- **CommerceLog**: registra la operación utilizando el mismo Trace ID para facilitar la observabilidad.

Gracias a esta arquitectura, todas las solicitudes protegidas siguen un flujo uniforme, permitiendo combinar seguridad, trazabilidad y logging de forma consistente en todos los microservicios.

---

### Responsabilidad de la Seguridad por Servicio

```
                    User Service
                 ──────────────────
                 • Login
                 • Registro
                 • Generación JWT
                 • Validación de credenciales
                          │
                          │ JWT
                          ▼
        ┌────────────────────────────────────┐
        │        Otros Microservicios        │
        ├────────────────────────────────────┤
        │ Product Service                    │
        │ Order Service                      │
        │ Notification Service               │
        └────────────────────────────────────┘
                 • Validan JWT
                 • Autorizan por roles
                 • Protegen endpoints
                 • No generan tokens
```

Este diseño mantiene una clara separación de responsabilidades:

- **User Service** es responsable de autenticar usuarios y emitir tokens JWT.
- **Los demás microservicios** validan el token recibido y aplican las reglas de autorización correspondientes.
- Ningún microservicio distinto de User Service genera nuevos tokens, lo que simplifica la arquitectura y centraliza el proceso de autenticación.