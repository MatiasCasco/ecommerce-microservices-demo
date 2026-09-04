# Order Service Domain

## Objetivo

Este documento describe el modelo de dominio de Order Service.

Su objetivo es definir:

- qué representa una `Order`;
- qué representa un `OrderItem`;
- el Aggregate;
- las invariantes;
- los estados;
- las transiciones permitidas;
- la inmutabilidad del historial.

Las decisiones y justificaciones arquitectónicas se documentan en `decisions.md`.

Los casos de uso se documentan en `use-cases.md`.

La seguridad, autorización y ownership se documentan en `security-authorization.md`.

---

# ¿Qué representa una Order?

Una `Order` representa el compromiso de compra realizado por un cliente.

No representa el catálogo ni el estado actual de los productos.

Representa la información histórica de la compra en el momento en que fue creada.

Una vez creada, debe conservar la información necesaria para reconstruir lo que fue comprado aunque el catálogo cambie posteriormente.

```text
Product Service
      │
      │ estado actual
      ▼
ProductCatalog
      │
      │ snapshot
      ▼
OrderItem
      │
      ▼
Order
      │
      └── historial de la compra
```

---

# Modelo Operacional vs Modelo Histórico

## Modelo operacional

`Product Service` representa el estado actual del catálogo.

Por ejemplo:

- nombre actual;
- precio actual;
- stock actual;
- estado actual.

```text
Product Service
      │
      ▼
Estado actual del producto
```

## Modelo histórico

`Order Service` conserva el estado comercial registrado al momento de la compra.

```text
ProductCatalog
      │
      │ información vigente
      ▼
OrderItem
      │
      │ snapshot histórico
      ▼
Order
```

Una Order existente no depende del catálogo actual para reconstruir la compra.

---

# Snapshot

`OrderItem` conserva un snapshot de la información comercial del producto.

Ejemplo:

```text
Día 1

Notebook Lenovo
Precio: Gs. 4.500.000
        │
        ▼
      Compra
        │
        ▼
    OrderItem
        │
        ├── productId
        ├── productName
        ├── unitPrice
        ├── quantity
        └── subtotal
```

Si posteriormente el producto cambia:

```text
Día 10

Notebook Lenovo LOQ Gen 10
Precio: Gs. 5.300.000
```

la Order existente continúa conservando:

```text
Notebook Lenovo
Gs. 4.500.000
```

porque representa el estado comercial registrado al momento de la compra.

---

# Order Aggregate

`Order` es el **Aggregate Root**.

```text
Order
│
├── id
├── customerId
├── items
│    ├── OrderItem
│    ├── OrderItem
│    └── ...
│
├── total
├── status
├── createdAt
└── updatedAt
```

`OrderItem` pertenece al Aggregate y no se manipula independientemente.

El Aggregate es responsable de proteger sus propias invariantes, calcular el total y controlar sus transiciones de estado.

El `id` de `Order` es generado por la base de datos y es incremental. La estrategia concreta de generación pertenece a la infraestructura de persistencia y no forma parte de las reglas de negocio del dominio.

`createdAt` representa el momento de creación de la Order y es inmutable.

`updatedAt` representa la última actualización relevante del recurso. No forma parte del contenido comercial histórico.

El `total` es calculado por `Order` y posteriormente persistido como parte de su estado. No se recalcula a partir del catálogo actual para reconstruir una Order existente.

---

# OrderItem

`OrderItem` representa una línea de compra.

Campos principales:

- `productId`
- `productName`
- `unitPrice`
- `quantity`
- `subtotal`

Su responsabilidad principal es representar exactamente lo que fue comprado.

El subtotal se obtiene mediante:

```text
subtotal = unitPrice × quantity
```

El `OrderItem` conserva el precio y nombre del producto correspondientes al momento de la compra.

---

# Invariantes

## Order

Una Order debe cumplir:

1. Debe contener al menos un `OrderItem`.
2. Un `productId` no puede repetirse dentro de la misma Order.
3. `total` debe ser igual a la suma de los subtotales.
4. El contenido comercial es inmutable una vez creada.
5. Toda Order comienza en `PENDING_PAYMENT`.
6. `PENDING_PAYMENT` puede pasar a `PAID`.
7. `PENDING_PAYMENT` puede pasar a `CANCELLED`.
8. `PAID` es terminal en el MVP.
9. `CANCELLED` es terminal en el MVP.
10. Una Order nunca se elimina.

La unicidad de `productId` dentro de la Order se obtiene en el flujo de creación mediante la consolidación de productos duplicados. Por ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
        ↓
productId = 10, quantity = 5
```

La consolidación normaliza el input antes de construir los `OrderItem` y facilita futuras representaciones de la compra, especialmente Invoice.

## OrderItem

Cada OrderItem debe cumplir:

1. `quantity > 0`.
2. `unitPrice > 0`.
3. `subtotal = unitPrice × quantity`.
4. `productName` representa el nombre del producto al momento de la compra.
5. `unitPrice` representa el precio del producto al momento de la compra.

---

# Errores de Dominio

Las invariantes del dominio forman parte del comportamiento del modelo.

Cuando una operación sobre `Order` u `OrderItem` viola una invariante propia del dominio, el modelo debe rechazar la operación mediante una excepción propia del dominio.

Los errores de dominio definidos para este propósito son:

```text
InvalidOrderItem
InvalidOrder
```

`InvalidOrderItem` representa una violación de las invariantes propias de `OrderItem`, por ejemplo:

```text
quantity <= 0
unitPrice <= 0
```

`InvalidOrder` representa una violación de las invariantes propias de `Order`, por ejemplo:

```text
Order sin items
productId duplicado
transición de estado no permitida
```

Estas excepciones pertenecen al dominio y deben ser independientes de cualquier mecanismo externo.

El dominio no debe depender de:

* `BusinessException`;
* `ErrorCode`;
* HTTP status;
* Spring;
* Controllers;
* `GlobalExceptionHandler`;
* mecanismos de persistencia.

La traducción de estos errores al contrato externo de la aplicación corresponde a las capas superiores, particularmente al adapter HTTP cuando la operación se expone mediante REST.

Conceptualmente:

```text
Order / OrderItem
       │
       │ viola una invariante
       ▼
Domain Exception
       │
       ├── InvalidOrder
       └── InvalidOrderItem
```

La excepción expresa que el modelo no permite realizar la operación solicitada; no representa un error de transporte ni un fallo de infraestructura.

---

# Inmutabilidad del Historial

Una vez creada una Order, no se modifica su información comercial.

Son inmutables:

* `productId`;
* `productName`;
* `unitPrice`;
* `quantity`;
* `subtotal`;
* `total`;
* `customerId`.

La Order representa un documento histórico.

El estado de la Order sí puede cambiar, pero únicamente mediante las transiciones permitidas por el Aggregate.


---

# Estados de la Orden

El MVP define:

- `PENDING_PAYMENT`
- `PAID`
- `CANCELLED`

---

# State Machine

```text
                 ┌──────────┐
                 │          ▼
        ┌──────────────────────────┐
        │     PENDING_PAYMENT      │
        └────────────┬─────────────┘
                     │
              ┌──────┴──────┐
              │             │
           pay()         cancel()
              │             │
              ▼             ▼
        ┌──────────┐   ┌───────────┐
        │   PAID   │   │ CANCELLED │
        └──────────┘   └───────────┘
          terminal       terminal
```

El comportamiento del Aggregate representa las transiciones:

```text
Order
├── pay()
└── cancel()
```

No se debe permitir cambiar el estado arbitrariamente mediante setters.

---

# Construcción de la Order

La construcción de la Order utiliza información validada del producto para crear los snapshots de sus `OrderItem`.

Antes de construir los `OrderItem`, los productos solicitados se consolidan por `productId`.

```text
Request
  │
  ▼
Consolidar productId duplicados
  │
  ▼
ProductCatalog
  │
  ├── productName
  ├── price
  ├── availableStock
  └── status
  │
  ▼
OrderItem
  │
  ▼
Order
  │
  ├── id
  ├── customerId
  ├── items
  ├── total
  ├── PENDING_PAYMENT
  ├── createdAt
  └── updatedAt
```

Durante la construcción:

- se consolidan las cantidades de un mismo `productId`;
- se crean los `OrderItem`;
- cada `OrderItem` conserva `productName` y `unitPrice` como snapshot;
- cada `OrderItem` calcula su subtotal;
- `Order` calcula el total;
- se establece `PENDING_PAYMENT`.

El total calculado por `Order` forma parte del estado persistido de la Order.

Una vez creada la Order, sus items y su contenido comercial no se modifican en el MVP.

La validación de disponibilidad de stock pertenece al flujo de Application utilizando el `availableStock` conocido por `ProductCatalog`.

El dominio de `Order` no reserva ni confirma stock. La reserva y la coordinación de concurrencia pertenecen a una futura responsabilidad de Inventory / Reservation.

---

# Responsabilidades del Dominio

```text
Order
├── protege invariantes
├── administra OrderItems
├── calcula total
└── controla transiciones de estado

OrderItem
└── calcula subtotal
```

El dominio no es responsable de:

- consolidar el request HTTP;
- resolver el actor autenticado;
- determinar ownership;
- consultar `ProductCatalog`;
- persistir la Order;
- gestionar `Idempotency-Key`.

La consolidación de productos duplicados forma parte de la normalización del input en Application antes de construir el Aggregate.

La coordinación del caso de uso pertenece a Application y se documenta en `use-cases.md`.

---

# Límites del Dominio

El dominio no conoce:

- JWT;
- Spring Security;
- `SecurityContext`;
- RabbitMQ;
- REST;
- Controllers;
- Repositories;
- API Gateway.

La autenticación, autorización y ownership se documentan en `security-authorization.md`.

La integración con Product Service y la proyección `ProductCatalog` se documentan en:

- `product-catalog.md`;
- `synchronization.md`;
- `event-consumption.md`.

---

# Principio del Modelo

El objetivo del dominio es mantener una representación:

```text
válida
  +
consistente
  +
históricamente reconstruible
```

sin acoplar `Order` a la infraestructura que la rodea.
