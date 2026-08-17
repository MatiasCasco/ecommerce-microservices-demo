# Order Service - Security & Authorization

## Objetivo

Este documento define cómo Order Service representa al actor autenticado y cómo determina qué operaciones puede ejecutar.

El objetivo principal es separar:

```text
Authentication
        ↓
¿Quién es el actor?

Authorization
        ↓
¿Qué puede hacer?

Ownership
        ↓
¿Sobre qué recurso puede operar?

Domain Rules
        ↓
¿La operación es válida según el negocio?
```

---

# Separación de Responsabilidades

Authentication, Authorization, Ownership y Domain Rules representan conceptos diferentes.

## Authentication

Determina quién es el actor.

El actor autenticado proviene de la identidad representada por el JWT.

```text
JWT
 ↓
Spring Security
 ↓
Authenticated Actor
```

Order Service no debe introducir detalles de Spring Security o JWT dentro del dominio.

---

## Authorization

Determina si el actor puede intentar ejecutar una determinada operación.

Ejemplo:

```text
USER  → operaciones sobre sus Orders
ADMIN → operaciones administrativas permitidas
```

La autorización no determina si la operación es válida según las reglas internas de `Order`.

---

## Ownership

Determina sobre qué recurso puede operar un actor.

Para `USER`:

```text
authenticatedCustomerId == Order.customerId
```

Por lo tanto, la existencia de una Order no es suficiente para permitir el acceso.

Debe verificarse que el usuario sea propietario de la Order.

`ADMIN` puede acceder a Orders de otros customers según las capacidades autorizadas por el caso de uso.

---

## Domain Rules

Las reglas del dominio determinan si la operación es válida según el estado y las invariantes de la Order.

Ejemplo:

```text
PAID
  ↓
no puede cancelarse mediante cancel()
```

Por lo tanto:

```text
Authorization
      ↓
Application Use Case
      ↓
Domain Rule
```

Tener autorización no permite romper las invariantes del Aggregate.

---

# Actores

## USER

Un `USER` trabaja con sus propias Orders.

Capacidades definidas:

```text
USER
 │
 ├── Create own Order
 ├── Get own Order
 ├── List own Orders
 └── Cancel own eligible Order
```

Para estas operaciones, la identidad del customer se obtiene del actor autenticado.

---

## ADMIN

Un `ADMIN` posee capacidades administrativas adicionales.

Capacidades definidas:

```text
ADMIN
 │
 ├── Create Order for customer
 ├── Get any Order
 ├── List Orders
 ├── Filter by customer
 └── Cancel eligible Order
```

Estas capacidades no convierten al administrador en propietario de la Order.

---

# Actor vs Customer

Actor y customer no son necesariamente la misma entidad.

Ejemplo:

```text
USER

actorId      = 25
customerId   = 25
```

Pero un administrador puede crear una Order para otro customer:

```text
ADMIN

actorId      = 1
customerId   = 25
```

Por lo tanto:

```text
Actor ≠ Customer
```

El actor representa quién ejecuta la operación.

El `customerId` representa para quién se crea o a quién pertenece la Order.

---

# Resolución de customerId

La resolución depende del actor.

```text
              Authenticated Actor
                      │
              ┌───────┴───────┐
              │               │
             USER           ADMIN
              │               │
              ▼               ▼
       customerId        customerId
          from JWT        from request
```

## USER

El `customerId` se obtiene de la identidad autenticada.

El cliente no debe poder elegir libremente otro `customerId`.

Conceptualmente:

```text
customerId = authenticatedCustomerId
```

## ADMIN

El `ADMIN` puede especificar explícitamente el `customerId` para crear una Order para otro customer.

Esto no requiere necesariamente endpoints diferentes.

El contrato puede mantener:

```text
POST /orders
```

y el caso de uso determina cómo resolver el ownership según el actor.

---

# Ownership de una Order

Una vez creada una Order, su `customerId` es inmutable.

```text
Order
 │
 └── customerId
        │
        └── no cambia durante el lifecycle de la Order
```

El ownership se utiliza principalmente para determinar el acceso de `USER`.

```text
USER
 │
 ├── authenticatedCustomerId
 │
 ▼
Order.customerId
 │
 ├── iguales    → acceso permitido
 └── diferentes → acceso denegado
```

---

# Authorization vs Domain

La autorización y el dominio no deben mezclarse.

## Authorization pregunta:

```text
¿Este actor puede intentar ejecutar esta operación?
```

## Domain pregunta:

```text
¿La Order permite esta operación según su estado e invariantes?
```

Ejemplo:

```text
ADMIN
  │
  │ tiene autorización para cancelar
  ▼
Cancel Order Use Case
  │
  ▼
Order.cancel()
  │
  └── verifica las reglas del dominio
```

Por lo tanto, incluso `ADMIN` no puede:

- modificar el precio de una Order;
- modificar sus items;
- cambiar el `customerId` de una Order existente;
- marcar manualmente una Order como `PAID`;
- eliminar una Order;

si esas operaciones violan las reglas e invariantes definidas por el dominio.

---

# Relación con Spring Security

Spring Security pertenece a la capa de infraestructura/aplicación.

El dominio no debe conocer:

- JWT;
- `Authentication`;
- `SecurityContext`;
- clases de Spring Security.

El caso de uso recibe una representación del actor autenticado que permita tomar las decisiones de autorización necesarias.

```text
Spring Security
      │
      ▼
Authenticated Actor
      │
      ▼
Application Use Case
      │
      ├── Authorization
      │
      └── Domain
```

De esta forma el dominio permanece independiente del framework de seguridad.

---

# Relación con los Casos de Uso

Los casos de uso son el punto donde se coordinan:

```text
Authenticated Actor
        │
        ▼
Authorization
        │
        ▼
Ownership
        │
        ▼
Application Use Case
        │
        ▼
Order Aggregate
```

Los detalles de cada operación se documentan en `use-cases.md`.

---

# Responsabilidad de este Documento

Este documento define únicamente:

- actor autenticado;
- USER;
- ADMIN;
- ownership;
- resolución de `customerId`;
- autorización;
- separación entre seguridad y dominio.

No documenta:

- JWT en detalle;
- configuración de Spring Security;
- endpoints HTTP;
- reglas internas del Aggregate;
- implementación de los casos de uso.

Esos aspectos pertenecen a los documentos correspondientes.

---

# Evolución

El modelo de autorización debe permitir incorporar nuevas capacidades sin introducir reglas de seguridad dentro del dominio.

La arquitectura podrá evolucionar posteriormente con nuevas capacidades administrativas, nuevos actores o nuevas políticas de autorización, manteniendo la separación:

```text
Authentication
      ↓
Authorization
      ↓
Ownership
      ↓
Use Case
      ↓
Domain
```
