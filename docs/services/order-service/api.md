# Order Service API

## Objetivo

Este documento describe la API pública de Order Service.

Su propósito es documentar los recursos, endpoints y comportamiento funcional expuesto por el servicio.

La especificación técnica del contrato HTTP corresponde a OpenAPI/Swagger.

---

# Base Path

```text
/orders
```

---

# Recursos

Order Service expone operaciones para:

- crear órdenes;
- consultar una orden;
- listar órdenes;
- gestionar el estado de una orden mediante las operaciones disponibles.

---

# Crear Orden

## POST /orders

Crea una nueva Order.

### Request

El request contiene la información necesaria para identificar los productos y cantidades solicitadas.

Para el ownership de la Order:

- `ROLE_USER`: el `customerId` se obtiene del actor autenticado y no se confía en un `customerId` enviado por el cliente.
- `ROLE_ADMIN`: el `customerId` debe ser proporcionado en el request.

Los productos se identifican mediante:

- `productId`;
- `quantity`.

El cliente no define:

- precio;
- subtotal;
- total.

### Flujo funcional

Durante la creación:

```text
Request
   │
   ▼
Validación
   │
   ▼
ProductCatalog
   │
   ├── producto existe
   ├── producto ACTIVE
   └── stock disponible
   │
   ▼
Order Aggregate
   │
   ├── OrderItems
   ├── subtotales
   └── total
   │
   ▼
Persistencia
   │
   ▼
PENDING_PAYMENT
```

La información del producto utilizada para construir los `OrderItem` proviene de `ProductCatalog`.

Order Service no realiza una consulta REST a Product Service durante este flujo.

### Estado inicial

Toda Order creada correctamente comienza en:

```text
PENDING_PAYMENT
```

### Eventos

Actualmente:

```text
No publica eventos.
```

La publicación de eventos de Order pertenece a una evolución futura.

---

# Obtener Orden

## GET /orders/{id}

Obtiene el detalle de una Order.

La respuesta representa el snapshot histórico de la compra.

La información de los `OrderItem` proviene de la propia Order.

No se consulta Product Service ni se reconstruye la información histórica desde el catálogo actual.

```text
GET /orders/{id}
       │
       ▼
     Order
       │
       ▼
Snapshot histórico
```

---

# Listar Órdenes

## GET /orders

Obtiene un listado paginado de Orders.

Características previstas:

- paginación;
- ordenamiento;
- filtros.

Los criterios concretos de filtrado se definirán cuando corresponda al caso de uso.

---

# Cancelar Orden

## PATCH /orders/{id}/cancel

**Estado: Futuro.**

Permitirá cancelar una Order cuando la transición sea válida según las reglas del dominio.

La transición definida actualmente es:

```text
PENDING_PAYMENT
       │
       │ cancel
       ▼
CANCELLED
```

No se permite cancelar una Order que ya se encuentre en un estado terminal.

---

# Estados de la Order

El modelo actual define:

| Estado | Descripción |
|---|---|
| `PENDING_PAYMENT` | Order creada esperando confirmación del pago. |
| `PAID` | Pago confirmado. |
| `CANCELLED` | Order cancelada. |

Las transiciones del dominio se encuentran documentadas en `domain.md`.

---

# Validaciones

Las validaciones relacionadas con la creación de una Order incluyen:

- cantidades válidas;
- existencia del producto;
- producto en estado `ACTIVE`;
- stock disponible.

Las reglas de negocio del Aggregate se encuentran en `domain.md`.

El detalle del caso de uso se documenta en `use-cases.md`.

---

# ProductCatalog

Order Service utiliza `ProductCatalog` como proyección local del catálogo.

```text
Product Service
      │
      │ eventos
      ▼
ProductCatalog
      │
      ▼
Order Service API
```

Order Service no consulta Product Service mediante REST durante la creación de una Order.

Los detalles de la proyección se documentan en:

- `product-catalog.md`;
- `synchronization.md`;
- `event-consumption.md`.

---

# Seguridad y Ownership

La API requiere autenticación.

La autenticación y autorización no forman parte del dominio de `Order`.

Las reglas de seguridad y ownership se documentan en:

```text
security-authorization.md
```

Conceptualmente:

```text
ROLE_USER
    │
    └── opera sobre sus propias Orders

ROLE_ADMIN
    │
    └── puede operar según las reglas de autorización definidas
```

El `customerId` de una Order no debe utilizarse para permitir que un `USER` opere sobre una Order perteneciente a otro Customer.

---

# Respuestas y Errores

Las respuestas exitosas y los errores seguirán un formato consistente con el resto del proyecto.

Los errores serán manejados mediante el mecanismo centralizado definido para Order Service y `common-lib`.

El contrato exacto de:

- status codes;
- response DTOs;
- error responses;

se define en la especificación técnica de la API y en la documentación correspondiente de errores.

---

# Eventos

Order Service actualmente consume eventos de Product Service para mantener `ProductCatalog`.

La API de Order todavía no publica eventos de ciclo de vida.

Como evolución futura podrán existir eventos como:

```text
ORDER_CREATED
ORDER_PAID
ORDER_CANCELLED
```

La publicación de estos eventos se documentará en `future.md`, `decisions.md` y la documentación de eventos cuando sea implementada.

---

# Documentación relacionada

Cada documento mantiene una responsabilidad específica:

- `domain.md` → modelo de dominio, invariantes y estados.
- `use-cases.md` → comportamiento de los casos de uso.
- `security-authorization.md` → autenticación, autorización y ownership.
- `order-flow.md` → flujos de negocio.
- `product-catalog.md` → proyección local del catálogo.
- `synchronization.md` → sincronización del catálogo.
- `event-consumption.md` → consumo de eventos.
- `decisions.md` → decisiones y trade-offs arquitectónicos.
- `roadmap.md` → evolución por etapas.
- `future.md` → capacidades futuras.

La documentación técnica definitiva de los contratos HTTP corresponde a OpenAPI/Swagger.
