# Order Creation Flow

## Objetivo

Este documento describe el flujo funcional de creación de una Order dentro de Order Service.

El flujo se centra exclusivamente en la creación de la Order y en la interacción entre:

- API;
- Application;
- ProductCatalog;
- Domain;
- Persistencia.

La sincronización de `ProductCatalog` se documenta en `product-catalog.md`, `synchronization.md` y `event-consumption.md`.

---

# Flujo Actual

```text
                    Cliente
                       │
                       ▼
                  POST /orders
                       │
                       ▼
                Order Controller
                       │
                       ▼
              Create Order Use Case
                       │
                       ▼
              Resolver customerId
                       │
                       ▼
              Consultar ProductCatalog
                       │
                       ▼
                Validar productos
                       │
             ┌─────────┼─────────┐
             │         │         │
             ▼         ▼         ▼
          ¿Existe?  ¿ACTIVE?  ¿Stock?
             │         │         │
             └─────────┴─────────┘
                       │
                       ▼
               Construir OrderItem
                       │
                       ▼
                 Construir Order
                       │
                       ▼
              Calcular subtotales
                       │
                       ▼
                 Calcular total
                       │
                       ▼
              Persistir Order
                + OrderItems
                       │
                       ▼
              PENDING_PAYMENT
                       │
                       ▼
                  HTTP 201
```

---

# 1. Recepción de la solicitud

El cliente solicita la creación de una nueva Order mediante:

```text
POST /orders
```

El request contiene los productos y cantidades solicitadas.

El `customerId` depende del actor:

```text
USER
 │
 └── customerId obtenido del actor autenticado


ADMIN
 │
 └── customerId proporcionado en el request
```

El cliente no controla los valores económicos de la Order.

No se aceptan como valores confiables:

- precio;
- subtotal;
- total.

---

# 2. Resolución del Customer

Antes de construir la Order, Application determina el `customerId` que será asociado al Aggregate.

```text
AuthenticatedActor
        │
        ▼
Create Order Use Case
        │
        ▼
customerId
        │
        ▼
Order
```

Las reglas de autenticación, autorización y ownership se documentan en:

```text
security-authorization.md
```

---

# 3. Consulta del ProductCatalog

Order Service consulta la proyección local:

```text
ProductCatalog
```

No realiza una llamada REST a Product Service durante la creación.

```text
Create Order
      │
      ▼
ProductCatalog
      │
      ├── productId
      ├── productName
      ├── price
      ├── availableStock
      └── status
```

`Product Service` continúa siendo el Source of Truth.

La proyección local se mantiene mediante eventos.

Los detalles se documentan en:

- `product-catalog.md`;
- `synchronization.md`;
- `event-consumption.md`.

---

# 4. Validación de productos

Para cada producto solicitado se verifica:

- existencia;
- estado `ACTIVE`;
- stock disponible;
- cantidad válida.

Conceptualmente:

```text
Requested Product
       │
       ├── ¿Existe?
       │
       ├── ¿Está ACTIVE?
       │
       ├── ¿Cantidad válida?
       │
       └── ¿Stock suficiente?
              │
              ▼
        Producto válido
```

Si alguna validación falla:

```text
No se crea la Order.
```

---

# 5. Construcción del OrderItem

Para cada producto válido se construye un `OrderItem`.

```text
ProductCatalog
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

El `unitPrice` utilizado es el precio conocido en `ProductCatalog`.

El cliente no puede sustituirlo por un precio enviado en el request.

El `OrderItem` representa un snapshot histórico.

---

# 6. Construcción del Aggregate Order

Los `OrderItem` se agrupan dentro del Aggregate Root `Order`.

```text
Order
│
├── customerId
│
├── OrderItem
├── OrderItem
├── ...
│
├── total
│
└── PENDING_PAYMENT
```

Durante esta etapa el Aggregate protege sus invariantes.

Las invariantes y reglas del dominio se encuentran en:

```text
domain.md
```

---

# 7. Cálculo de importes

Cada `OrderItem` calcula su subtotal:

```text
subtotal = unitPrice × quantity
```

Después `Order` calcula el total:

```text
total = Σ subtotales
```

```text
OrderItem
    │
    └── subtotal
          │
          ▼
        Order
          │
          └── total
```

El cálculo pertenece al dominio.

---

# 8. Persistencia

Una vez construido y validado el Aggregate:

```text
Order
  +
OrderItems
```

se persisten como una única unidad transaccional.

```text
BEGIN TRANSACTION
        │
        ▼
Persist Order
        │
        ▼
Persist OrderItems
        │
   ┌────┴────┐
   │         │
   ▼         ▼
  OK       ERROR
   │         │
   ▼         ▼
COMMIT    ROLLBACK
```

No debe quedar una Order parcialmente persistida respecto de sus Items.

---

# 9. Estado Inicial

Toda Order creada correctamente comienza en:

```text
PENDING_PAYMENT
```

La Order no nace pagada.

El pago representa un proceso independiente.

---

# 10. Resultado

Si todo el flujo finaliza correctamente:

```text
Order
├── customerId
├── items
├── total
└── PENDING_PAYMENT
```

La API responde con:

```text
HTTP 201 Created
```

La respuesta y el contrato HTTP se documentan en:

```text
api.md
```

---

# Flujo ante errores

Si una validación de negocio falla:

```text
Request
   │
   ▼
Validación
   │
   ▼
ERROR
   │
   ▼
No se crea Order
```

Si ocurre un error durante la persistencia:

```text
BEGIN TRANSACTION
        │
        ▼
      ERROR
        │
        ▼
    ROLLBACK
```

No se debe considerar creada una Order cuya transacción no haya finalizado correctamente.

---

# Evolución del flujo

Actualmente el flujo de creación no publica eventos propios de Order.

```text
POST /orders
      │
      ▼
Create Order
      │
      ▼
Persist Order
      │
      ▼
PENDING_PAYMENT
```

La evolución prevista incorpora publicación de eventos:

```text
Cliente
   │
   ▼
POST /orders
   │
   ▼
Create Order
   │
   ▼
Persist Order
   │
   ▼
OrderCreatedEvent
   │
   ▼
RabbitMQ
   │
   ├────────► Notification Service
   │
   ├────────► Payment Service
   │
   └────────► Inventory Service
```

Esta evolución no forma parte todavía del flujo implementado.

La publicación de eventos y sus decisiones asociadas se documentarán en los documentos correspondientes cuando se implemente.

---

# Principios del Flujo

El flujo actual respeta:

- Aggregate Root;
- Domain-Driven Design;
- Local Projection;
- Event-Driven Architecture para sincronización del catálogo;
- consistencia transaccional;
- bajo acoplamiento entre microservicios;
- separación entre Application y Domain.

La responsabilidad de cada capa se mantiene separada:

```text
API
 │
 └── recibe la solicitud

Application
 │
 └── coordina el caso de uso

ProductCatalog
 │
 └── proporciona información actual del producto

Domain
 │
 └── protege invariantes y calcula importes

Persistence
 │
 └── conserva el Aggregate
```
