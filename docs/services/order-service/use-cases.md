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
- `Idempotency-Key` está presente.
- Los items están presentes en el request.
- El customer requerido por el caso de uso es válido.
- Cada producto existe en `ProductCatalog`.
- Cada producto está `ACTIVE`.
- Existe stock suficiente según `ProductCatalog`.

Las invariantes propias de `Order` y `OrderItem` son protegidas por el dominio durante la construcción del Aggregate.

---

## Input

El contrato HTTP utiliza:

```text
CreateOrderRequest
├── customerId?
└── items[]
     └── CreateOrderItemRequest
         ├── productId
         └── quantity
```

Application transforma el DTO HTTP en un comando interno, conceptualmente:

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

El `Idempotency-Key` se recibe desde la capa API y forma parte de la coordinación de idempotencia del caso de uso.

El dominio no recibe información sobre JWT, roles, Spring Security ni `Idempotency-Key`.

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

Antes de construir los `OrderItem`, Application normaliza los items consolidando cantidades del mismo `productId`.

Ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
        ↓
productId = 10, quantity = 5
```

Los duplicados no son un error.

Para cada producto normalizado:

```text
product exists
        +
status = ACTIVE
        +
availableStock >= requested quantity
```

La validación de stock utiliza el stock conocido por `ProductCatalog` y no constituye una reserva.

La reserva de inventario pertenece a una futura responsabilidad de Inventory / Reservation.

### Precios

El cliente no define:

- `unitPrice`;
- `subtotal`;
- `total`.

Los valores económicos se obtienen de `ProductCatalog`.

`OrderItem.unitPrice` conserva el precio conocido como snapshot histórico.

`Order` calcula el total y dicho total forma parte del estado persistido.

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

Para cada producto normalizado y validado:

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
├── id
├── customerId
├── OrderItems
├── total
├── PENDING_PAYMENT
├── createdAt
└── updatedAt
```

El dominio:

- construye `Order`;
- construye `OrderItem`;
- conserva `productName` y `unitPrice` como snapshot;
- calcula subtotales;
- calcula el total;
- protege sus invariantes;
- controla las transiciones de estado.

La generación del `id` incremental pertenece a la persistencia y es gestionada por la base de datos.

Las violaciones de invariantes del dominio producen excepciones propias del dominio.

Por ejemplo:

- `InvalidOrderItem` cuando un `OrderItem` recibe una cantidad o precio inválido;
- `InvalidOrder` cuando la construcción o modificación de `Order` viola una invariante del Aggregate.

Estas excepciones pertenecen al dominio y no dependen de HTTP, Spring, `BusinessException`, `ErrorCode` ni otros mecanismos de infraestructura o transporte.

---

## Persistencia

La Order, sus Items y el registro necesario para garantizar la idempotencia se persisten como una única unidad transaccional.

Conceptualmente:

```text
BEGIN TRANSACTION
        │
        ├── Order
        │
        ├── OrderItems
        │
        └── IdempotencyRecord
        │
        ▼
      COMMIT
```

Si falla una parte:

```text
ROLLBACK
```

No debe existir una Order incompleta ni una Order creada sin la información necesaria para garantizar la idempotencia.

El `total` calculado por `Order` también se persiste dentro de la misma transacción.

---

## Resultado

Una creación exitosa produce:

```text
Order
│
├── id
├── customerId
├── items
├── total
├── status = PENDING_PAYMENT
├── createdAt
└── updatedAt
```

La API responde con `HTTP 201 Created` y devuelve un `OrderResponse`.

Si se repite una solicitud con la misma `Idempotency-Key` y el mismo request, se devuelve el resultado de la operación original sin crear una nueva Order.

Si la misma `Idempotency-Key` se utiliza con un request diferente, la operación se rechaza.

---

## Errores relevantes

### Errores de Request / API

La capa HTTP rechaza la operación cuando:

- falta `Idempotency-Key`;
- el request tiene una estructura inválida;
- faltan campos requeridos;
- el request contiene datos con formato inválido.

### Errores de Application

El caso de uso rechaza la operación cuando:

- el actor no está autorizado;
- un `USER` intenta utilizar otro `customerId`;
- el producto no existe;
- el producto está `INACTIVE`;
- el stock conocido es insuficiente;
- una `Idempotency-Key` existente se utiliza con un request diferente.

### Errores de Domain

Durante la construcción del Aggregate, el dominio rechaza cualquier estado que viole sus invariantes.

Por ejemplo:

- `OrderItem` con cantidad menor o igual a cero;
- `OrderItem` con precio menor o igual a cero;
- construcción de una `Order` sin items;
- `Order` con `productId` duplicado;
- transición de estado inválida.

Estos errores se representan mediante excepciones propias del dominio, como `InvalidOrderItem` e `InvalidOrder`.

El caso de uso no duplica estas reglas ni las convierte en errores de infraestructura.

### Idempotencia

Una `Idempotency-Key` repetida con el mismo request no es un error: representa el mismo intento lógico y devuelve el resultado original.

Una `Idempotency-Key` existente utilizada con un request diferente constituye un error de Application.

### Persistencia

Si falla la persistencia, la transacción debe revertirse.

Un fallo de persistencia no constituye un error de negocio ni una excepción del dominio.

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

Se devuelve:

```text
OrderResponse
```

La información histórica de `OrderItem` proviene de la Order almacenada.

No se reconstruye la representación desde el estado actual de `ProductCatalog`.

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

Se devuelve:

```text
Page<OrderSummaryResponse>
```

`OrderSummaryResponse` es una representación resumida y no incluye los `OrderItem`.

El detalle completo se obtiene mediante `Get Order`.

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

Si la transición no es válida, el Aggregate produce una excepción propia del dominio.

Application no modifica ni interpreta directamente el estado interno de la Order para forzar la transición.

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

La respuesta del caso de uso se representa mediante `OrderResponse`.

El caso de uso no modifica directamente `status`; invoca el comportamiento del Aggregate.

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
