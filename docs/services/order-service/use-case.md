# Order Service Use Cases

## Objetivo

Este documento define los casos de uso de Order Service.

Cada caso de uso describe:

- Actor;
- precondiciones;
- input;
- reglas;
- dependencias;
- interacción con el dominio;
- persistencia;
- resultado;
- errores relevantes.

El objetivo es documentar el comportamiento de Application sin duplicar las reglas internas del dominio.

Las invariantes y transiciones de `Order` se encuentran en `domain.md`.

Las reglas de autenticación, autorización y ownership se encuentran en `security-authorization.md`.

---

# Casos de Uso

```text
Order Service
│
├── Create Order
├── Get Order
├── List Orders
├── Cancel Order
│
└── Future
    └── Pay Order
```

---

# Create Order

## Objetivo

Crear una nueva Order a partir de los productos y cantidades solicitados por el actor.

---

## Actor

```text
USER / ADMIN
```

La resolución del `customerId` depende del actor:

```text
USER
 │
 └── customerId ← authenticated actor

ADMIN
 │
 └── customerId ← request
```

Un `USER` no puede crear una Order para otro customer.

Un `ADMIN` puede crear una Order para un customer especificado.

---

## Precondiciones

- El actor está autenticado.
- El actor está autorizado para crear una Order.
- Los items están presentes.
- Cada item tiene una cantidad válida.
- El customer requerido por el caso de uso es válido.
- Cada producto existe en `ProductCatalog`.
- Cada producto está `ACTIVE`.
- Existe stock suficiente según `ProductCatalog`.

La validación de customer no implica actualmente una `CustomerProjection`; esa evolución se contempla como una posibilidad futura.

---

## Input

Conceptualmente:

```text
CreateOrderCommand
│
├── customerId
├── items
│    ├── productId
│    └── quantity
└── actor
```

El `actor` permite al caso de uso aplicar las reglas de autorización y ownership.

El dominio no recibe información sobre JWT, roles o Spring Security.

---

## Reglas

### Customer

```text
USER
    customerId = authenticatedCustomerId

ADMIN
    customerId = request.customerId
```

### Product

Para cada producto:

```text
product exists
        +
status = ACTIVE
        +
availableStock >= requested quantity
```

### Precios

El cliente no define:

- `unitPrice`;
- `subtotal`;
- `total`.

Los valores económicos se obtienen de `ProductCatalog`.

---

## Dependencias

El caso de uso utiliza:

```text
ProductCatalog
```

como fuente local de información operacional.

No consulta Product Service mediante REST durante la creación.

```text
Create Order
      │
      ▼
ProductCatalog
      │
      ▼
Order Aggregate
```

---

## Dominio

Para cada producto válido:

```text
ProductCatalog
      │
      ▼
OrderItem
```

Se construye el Aggregate:

```text
Order
│
├── customerId
├── OrderItems
├── total
└── PENDING_PAYMENT
```

El dominio:

- construye `Order`;
- construye `OrderItem`;
- calcula subtotales;
- calcula el total;
- protege sus invariantes.

---

## Persistencia

La Order y sus Items se persisten como una única unidad transaccional.

```text
BEGIN TRANSACTION
        │
        ├── Order
        │
        └── OrderItems
        │
        ▼
      COMMIT
```

Si falla una parte:

```text
ROLLBACK
```

No debe existir una Order incompleta.

---

## Resultado

Una creación exitosa produce:

```text
Order
│
├── customerId
├── items
├── total
└── status = PENDING_PAYMENT
```

La API responde con `HTTP 201 Created`.

---

## Errores relevantes

El caso de uso debe rechazar la operación cuando:

- el actor no está autorizado;
- un `USER` intenta utilizar otro `customerId`;
- faltan items;
- la cantidad es inválida;
- el producto no existe;
- el producto está `INACTIVE`;
- el stock conocido es insuficiente.

Si falla la persistencia, la transacción debe revertirse.

---

# Get Order

## Objetivo

Obtener una Order existente.

---

## Actor

```text
USER / ADMIN
```

---

## Precondiciones

- El actor está autenticado.
- El actor está autorizado para consultar Orders.
- La Order existe.

---

## Reglas de acceso

### USER

Solo puede consultar una Order cuando:

```text
authenticatedCustomerId == Order.customerId
```

### ADMIN

Puede consultar cualquier Order según las capacidades administrativas del MVP.

```text
ADMIN → cualquier Order
```

---

## Dominio

La consulta no modifica el Aggregate.

La Order representa su propio snapshot histórico.

No se consulta Product Service para reconstruir los datos.

---

## Persistencia

Se obtiene la Order desde el repositorio correspondiente.

```text
Order Repository
       │
       ▼
     Order
```

---

## Resultado

Se devuelve la representación de la Order solicitada.

La información histórica de `OrderItem` proviene de la Order almacenada.

---

## Errores relevantes

- actor no autenticado;
- actor no autorizado;
- USER intentando consultar una Order de otro customer;
- Order inexistente.

---

# List Orders

## Objetivo

Consultar Orders de acuerdo con las capacidades del actor.

---

## Actor

```text
USER / ADMIN
```

---

## USER

Un `USER` puede consultar solamente sus propias Orders.

Conceptualmente:

```text
USER
 │
 └── authenticatedCustomerId
             │
             ▼
       Orders.customerId
```

El resultado debe limitarse a las Orders pertenecientes al customer autenticado.

---

## ADMIN

Un `ADMIN` puede consultar Orders según las capacidades administrativas definidas para el MVP.

Actualmente:

```text
ADMIN
 │
 ├── List Orders
 └── Filter by customer
```

---

## Input

Los parámetros de consulta pueden incluir:

- paginación;
- ordenamiento;
- filtros soportados por la API.

El detalle del contrato HTTP se encuentra en `api.md`.

---

## Reglas

La autorización y ownership se aplican antes de devolver los resultados.

Un filtro enviado por un `USER` no debe permitirle acceder a Orders de otro customer.

---

## Resultado

Se devuelve una colección paginada de Orders.

---

# Cancel Order

## Objetivo

Cancelar una Order cuando la transición sea válida según el dominio.

---

## Actor

```text
USER / ADMIN
```

---

## Precondiciones

- El actor está autenticado.
- El actor está autorizado para cancelar.
- La Order existe.
- El actor puede operar sobre la Order según ownership/autorización.

---

## Reglas de autorización

### USER

Puede intentar cancelar una Order propia.

```text
authenticatedCustomerId == Order.customerId
```

### ADMIN

Puede intentar cancelar una Order según sus capacidades administrativas.

---

## Regla de dominio

La autorización no determina por sí sola si la cancelación es válida.

El Aggregate debe decidirlo.

```text
PENDING_PAYMENT
       │
       │ cancel()
       ▼
CANCELLED
```

Una Order en `PAID` no puede cancelarse mediante la transición definida actualmente.

```text
PAID
  │
  └── cancel() → ❌
```

---

## Flujo

```text
Actor
  │
  ▼
Authorization
  │
  ▼
Get Order
  │
  ▼
Ownership
  │
  ▼
Order.cancel()
  │
  ├── válida
  │     │
  │     ▼
  │   Persist
  │
  └── inválida
        │
        ▼
       Error
```

El caso de uso nunca debe cambiar directamente el estado mediante un setter para evitar las reglas del Aggregate.

---

## Resultado

La Order queda:

```text
CANCELLED
```

si la transición fue válida.

---

# Future: Pay Order

## Estado

```text
FUTURO
```

Actualmente `PAID` forma parte del modelo de estado, pero el pago no es una operación administrativa del MVP.

La evolución prevista es:

```text
Payment Service
      │
      ▼
PaymentConfirmedEvent
      │
      ▼
Order Service
      │
      ▼
Order
      │
      ▼
PAID
```

Esto evita que un `ADMIN` pueda marcar manualmente una Order como `PAID`.

`PAID` representa un pago confirmado, no una decisión administrativa.

---

# Separación de Responsabilidades

El caso de uso coordina, pero no reemplaza al dominio.

```text
Application
│
├── recibe Actor
├── aplica Authorization
├── resuelve customerId
├── consulta ProductCatalog
├── coordina persistencia
│
└── invoca
       │
       ▼
     Domain
       │
       ├── invariantes
       ├── cálculos
       └── transiciones
```

La separación fundamental es:

```text
Authorization
      ↓
¿Puede intentar la operación?

Domain
      ↓
¿La operación es válida?
```

Una autorización administrativa nunca permite romper las invariantes del Aggregate.
