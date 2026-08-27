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

### Idempotencia

La creación de una Order requiere obligatoriamente el header:

```http
Idempotency-Key: <unique-key>
```

La clave identifica un intento lógico de creación.

Si se repite una solicitud con:

```text
misma Idempotency-Key
+
mismo request
```

se considera el mismo intento lógico y no se crea una nueva Order.

Si una `Idempotency-Key` previamente utilizada llega asociada a un request diferente, la solicitud debe ser rechazada.

La persistencia necesaria para garantizar la idempotencia se realiza en PostgreSQL y debe mantener consistencia transaccional con la creación de:

```text
Order
+
OrderItems
```

### Request

El contrato HTTP utiliza un único DTO para USER y ADMIN:

```text
CreateOrderRequest
├── customerId?
└── items[]
    └── CreateOrderItemRequest
        ├── productId
        └── quantity
```

Para el ownership de la Order:

- `ROLE_USER`: el `customerId` se obtiene del actor autenticado y no se confía en un `customerId` enviado por el cliente.
- `ROLE_ADMIN`: el `customerId` debe ser proporcionado en el request cuando la autorización lo permita.

Los productos se identifican mediante:

- `productId`;
- `quantity`.

El cliente no define:

- `price`;
- `unitPrice`;
- `subtotal`;
- `total`;
- `status`;
- `createdAt`;
- `updatedAt`.

### Normalización de productos duplicados

Si el request contiene más de una línea para el mismo `productId`, las cantidades se consolidan antes de construir los `OrderItem`.

Ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
        ↓
productId = 10, quantity = 5
```

La Order resultante contiene como máximo un `OrderItem` por `productId`.

Esto facilita también futuras representaciones de la compra, especialmente Invoice.

### Flujo funcional

Durante la creación:

```text
Request
   │
   ▼
Validar Idempotency-Key
   │
   ▼
Resolver Actor / customerId
   │
   ▼
Consolidar productos duplicados
   │
   ▼
ProductCatalog
   │
   ├── producto existe
   ├── producto ACTIVE
   └── stock conocido suficiente
   │
   ▼
Order Aggregate
   │
   ├── OrderItems
   ├── subtotales
   └── total
   │
   ▼
Persistencia transaccional
   │
   ├── Order
   ├── OrderItems
   └── IdempotencyRecord
   │
   ▼
COMMIT
   │
   ▼
OrderResponse
```

La información del producto utilizada para construir los `OrderItem` proviene de `ProductCatalog`.

Order Service no realiza una consulta REST a Product Service durante este flujo.

La validación de stock utiliza el `availableStock` conocido por `ProductCatalog`. Esta validación no constituye una reserva de inventario.

### Estado inicial

Toda Order creada correctamente comienza en:

```text
PENDING_PAYMENT
```

El `total` es calculado por `Order` y persistido como parte del estado de la Order.

### Response

Una creación exitosa devuelve:

```text
OrderResponse
```

con:

```text
OrderResponse
├── id
├── customerId
├── items[]
│   └── OrderItemResponse
│       ├── productId
│       ├── productName
│       ├── unitPrice
│       ├── quantity
│       └── subtotal
├── total
├── status
├── createdAt
└── updatedAt
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

### Response

La respuesta utiliza:

```text
OrderResponse
```

```text
OrderResponse
├── id
├── customerId
├── items[]
│   └── OrderItemResponse
│       ├── productId
│       ├── productName
│       ├── unitPrice
│       ├── quantity
│       └── subtotal
├── total
├── status
├── createdAt
└── updatedAt
```

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
OrderResponse
       │
       ▼
Snapshot histórico
```

---

# Listar Órdenes

## GET /orders

Obtiene un listado paginado de Orders.

### Response

La respuesta utiliza:

```text
Page<OrderSummaryResponse>
```

`OrderSummaryResponse` representa una versión resumida de la Order y no incluye `items`.

```text
OrderSummaryResponse
├── id
├── customerId
├── total
├── status
├── createdAt
└── updatedAt
```

### Características

- paginación;
- ordenamiento;
- filtro por `customerId`.

### Filtro por customerId

El parámetro `customerId` permite filtrar las Orders según el actor autenticado.

```http
GET /orders?customerId=25
```
Las reglas son:


```text
USER
 │
 └── customerId se obtiene del actor autenticado
     y no puede utilizarse para consultar Orders de otro customer.


ADMIN
 │
 └── puede utilizar customerId para filtrar Orders
     de un customer específico.
```

---

# Cancelar Orden

## PATCH /orders/{id}/cancel

Permite cancelar una Order cuando la transición sea válida según las reglas del dominio.

La transición definida actualmente es:

```text
PENDING_PAYMENT
       │
       │ cancel()
       ▼
CANCELLED
```

No se permite cancelar una Order que ya se encuentre en un estado terminal.

La operación no recibe un nuevo estado desde el cliente. La transición es ejecutada por el comportamiento del Aggregate.

### Response

Una cancelación exitosa devuelve:

```text
OrderResponse
```

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

- `Idempotency-Key` obligatorio;
- cantidades válidas;
- existencia del producto;
- producto en estado `ACTIVE`;
- stock conocido suficiente;
- consolidación de productos duplicados.

La validación de stock no representa una reserva de inventario.

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

Las respuestas exitosas utilizan los DTOs definidos por cada operación:

```text
POST /orders
    → OrderResponse

GET /orders/{id}
    → OrderResponse

GET /orders
    → Page<OrderSummaryResponse>

PATCH /orders/{id}/cancel
    → OrderResponse
```

Los errores serán manejados mediante el mecanismo centralizado definido para Order Service y `common-lib`.

Entre los errores funcionales relevantes se encuentran:

- `OrderNotFound`;
- `OrderNotCancellable`;
- `ProductNotFound`;
- `ProductInactive`;
- `InsufficientStock`;
- `InvalidOrderItem`;
- errores relacionados con `Idempotency-Key`.

El contrato técnico definitivo de status codes, schemas y error responses corresponde a OpenAPI/Swagger y a la documentación de errores.

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
- `use-cases.md` → comportamiento de los casos de uso y coordinación de Application.
- `security-authorization.md` → autenticación, autorización y ownership.
- `order-flow.md` → flujos de negocio.
- `product-catalog.md` → proyección local del catálogo.
- `synchronization.md` → sincronización del catálogo.
- `event-consumption.md` → consumo de eventos.
- `decisions.md` → decisiones y trade-offs arquitectónicos.
- `roadmap.md` → evolución por etapas.
- `future.md` → capacidades futuras.

La documentación técnica definitiva de los contratos HTTP corresponde a OpenAPI/Swagger.
