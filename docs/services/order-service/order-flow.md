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
                 Validar Idempotency-Key
                            │
                            ▼
                Create Order Use Case
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
                ┌───────────┼───────────┐
                ▼           ▼           ▼
             ¿Existe?    ¿ACTIVE?    ¿Stock?
                │           │           │
                └───────────┴───────────┘
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
                 Persistencia transaccional
                            │
                 ┌──────────┼──────────┐
                 ▼          ▼          ▼
               Order    OrderItems   IdempotencyRecord
                 │          │          │
                 └──────────┴──────────┘
                            │
                            ▼
                          COMMIT
                            │
                            ▼
                     PENDING_PAYMENT
                            │
                            ▼
                        HTTP 201
```

La creación de `Order`, sus `OrderItem` y la información necesaria para garantizar la idempotencia forman parte de una operación transaccional consistente.

---

# 1. Recepción de la solicitud

El cliente solicita la creación de una nueva Order mediante:

```text
POST /orders
```

La solicitud debe incluir obligatoriamente:

```http
Idempotency-Key: <unique-key>
```

El request contiene los productos y cantidades solicitadas.

El contrato HTTP utiliza un único `CreateOrderRequest` para USER y ADMIN.

Para el ownership de la Order:

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
- unitPrice;
- subtotal;
- total;
- status;
- createdAt;
- updatedAt.

Si la misma `Idempotency-Key` se reutiliza con el mismo request, se considera el mismo intento lógico y no se crea una nueva Order.

Si la misma `Idempotency-Key` se utiliza con un request diferente, la solicitud debe ser rechazada.

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

# 3.1. Normalización de productos duplicados

Antes de construir los `OrderItem`, Application consolida las líneas que tengan el mismo `productId`.

Ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
        ↓
productId = 10, quantity = 5
```

El resultado normalizado contiene como máximo una línea por `productId`.

La consolidación no es un error de negocio.

Su objetivo es mantener la invariante del Aggregate y facilitar futuras representaciones de la Order, especialmente Invoice.

```text
Request items
      │
      ▼
Consolidación por productId
      │
      ▼
Items normalizados
      │
      ▼
ProductCatalog
```

---

# 4. Validación de productos

Para cada producto normalizado, Application verifica las reglas que dependen de `ProductCatalog`:

* existencia;
* estado `ACTIVE`;
* stock conocido suficiente.

Conceptualmente:

```text
Requested Product
       │
       ├── ¿Existe?
       │
       ├── ¿Está ACTIVE?
       │
       └── ¿Stock conocido suficiente?
              │
              ▼
         Producto válido
```

La cantidad solicitada forma parte de la entrada que posteriormente será utilizada para construir el `OrderItem`.

La validez de la cantidad como invariante del dominio:

```text
quantity > 0
```

es responsabilidad de `OrderItem` y se protege durante su construcción.

Por lo tanto, Application no duplica la regla `quantity > 0` como una regla propia del caso de uso.

Si una validación de Application falla:

```text
No se crea la Order.
```

Si una invariante del dominio falla durante la construcción del Aggregate:

```text
No se construye un Aggregate inválido.
```

La validación de stock utiliza el `availableStock` conocido por `ProductCatalog`.

Esta validación no representa una reserva de inventario.

La reserva y la coordinación de concurrencia pertenecen a una futura responsabilidad de Inventory / Reservation.

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

Una vez construido y validado el Aggregate, se persisten como una única unidad transaccional:

```text
Order
  +
OrderItems
  +
IdempotencyRecord
```

Conceptualmente:

```text
BEGIN TRANSACTION
        │
        ▼
Persist Order
        │
        ▼
Persist OrderItems
        │
        ▼
Persist IdempotencyRecord
        │
   ┌────┴────┐
   │         │
   ▼         ▼
  OK       ERROR
   │         │
   ▼         ▼
COMMIT    ROLLBACK
```

No debe quedar una Order parcialmente persistida respecto de sus Items ni debe quedar una Order creada sin la información necesaria para garantizar la idempotencia.

La persistencia del `total` forma parte de la misma operación.

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
├── id
├── customerId
├── items
├── total
├── PENDING_PAYMENT
├── createdAt
└── updatedAt
```

La API responde con:

```text
HTTP 201 Created
```

y devuelve un:

```text
OrderResponse
```

La respuesta y el contrato HTTP se documentan en:

```text
api.md
```

---

# Flujo ante errores

Los errores se manejan según la responsabilidad de la capa en la que se origina la condición.

```text
API
 │
 ├── errores del contrato HTTP
 │
 ▼
Application
 │
 ├── errores del caso de uso
 │
 ▼
Domain
 │
 └── violaciones de invariantes
```

## Error de validación del Request

Si el request no cumple el contrato HTTP:

```text
Request
   │
   ▼
Validación HTTP
   │
   ▼
ERROR
   │
   ▼
No se ejecuta el caso de uso
```

Ejemplos:

* `Idempotency-Key` ausente;
* estructura inválida del request;
* campos requeridos ausentes;
* formato inválido de los datos.

Estos errores pertenecen al contrato de entrada y no son invariantes del dominio.

## Error de Application

Si una regla necesaria para coordinar el caso de uso no puede cumplirse:

```text
Request
   │
   ▼
Application
   │
   ▼
ERROR
   │
   ▼
No se crea Order
```

Ejemplos:

* producto inexistente;
* producto `INACTIVE`;
* stock conocido insuficiente;
* Order inexistente;
* Order no cancelable;
* acceso no autorizado;
* conflicto de idempotencia.

Estos errores pertenecen a Application porque requieren información o decisiones externas al Aggregate.

## Error de Domain

Durante la construcción o modificación del Aggregate, una invariante puede ser violada:

```text
Application
   │
   ▼
Order / OrderItem
   │
   ▼
Violación de invariante
   │
   ▼
Domain Exception
   │
   ▼
No se crea / modifica el Aggregate
```

Ejemplos:

```text
InvalidOrderItem
InvalidOrder
```

`InvalidOrderItem` puede producirse cuando se viola una invariante propia de `OrderItem`, por ejemplo:

```text
quantity <= 0
unitPrice <= 0
```

`InvalidOrder` puede producirse cuando se viola una invariante propia de `Order`, por ejemplo:

```text
Order sin items
productId duplicado
transición de estado no permitida
```

Estas excepciones pertenecen al dominio y no conocen HTTP, Spring, `BusinessException` ni `ErrorCode`.

## Idempotency-Key ya utilizada

Si la clave ya existe:

```text
Idempotency-Key
      │
      ▼
   ¿Existe?
   │       │
   │ NO    │ SÍ
   ▼       ▼
continuar  comparar request
              │
        ┌─────┴─────┐
        ▼           ▼
      mismo      diferente
        │           │
        ▼           ▼
 resultado        ERROR
 original
```

Si el request es el mismo, se devuelve el resultado de la operación original y no se crea otra Order.

Si el request es diferente, se rechaza la solicitud.

El conflicto por reutilización de una `Idempotency-Key` pertenece al flujo de Application y no constituye una invariante del Aggregate.

## Error de persistencia

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

Los fallos de infraestructura no deben convertirse artificialmente en errores de negocio.

La traducción de los errores de Domain y Application al contrato HTTP corresponde al adapter HTTP.

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
