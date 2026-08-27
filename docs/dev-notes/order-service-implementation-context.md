# Order Service — Contexto Maestro para iniciar la Implementación

> **Propósito de este documento:** trasladar el diseño cerrado de `order-service` a un nuevo chat para comenzar la implementación sin volver a discutir decisiones ya cerradas.
>
> **Regla principal:** no reabrir decisiones de diseño ya tomadas salvo que durante la implementación aparezca una contradicción real con el código, los documentos o una restricción técnica que obligue a revisarlas.

---

# 1. Estado del proyecto

Estamos construyendo un e-commerce basado en microservicios.

El `order-service` es responsable de representar y gestionar Orders.

Arquitectura general relevante:

```text
                         ┌─────────────────────┐
                         │    Product Service   │
                         │   Source of Truth    │
                         └──────────┬──────────┘
                                    │
                              Product Events
                                    │
                                    ▼
                              RabbitMQ
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Order Service    │
                         │                     │
                         │  ProductCatalog     │
                         │       +             │
                         │      Order          │
                         └─────────────────────┘
```

Order Service mantiene una proyección local de productos llamada `ProductCatalog`.

Product Service continúa siendo el Source of Truth del catálogo.

La sincronización Product Service → Order Service es event-driven y eventualmente consistente.

---

# 2. Objetivo del nuevo chat

El diseño de `order-service` fue revisado y cerrado.

El siguiente objetivo es:

```text
DISEÑO CERRADO
      │
      ▼
IMPLEMENTACIÓN
```

No debemos volver a diseñar desde cero:

- Order;
- OrderItem;
- total;
- precio;
- stock;
- customerId;
- DTOs;
- idempotencia;
- State Machine;
- ProductCatalog;
- ownership;
- desacoplamiento;
- futuras capacidades.

La implementación debe seguir las decisiones de este documento y de la documentación Markdown del componente.

---

# 3. Documentación oficial del componente

Los documentos principales de `order-service` son:

```text
README.md
decisions.md
domain.md
api.md
order-flow.md
use-case.md
security-authorization.md
product-catalog.md
synchronization.md
event-consumption.md
roadmap.md
future.md
```

Estos documentos representan el diseño.

Importante:

```text
use-case.md
```

es el nombre definitivo del documento de casos de uso.

No utilizar `use-cases.md`.

---

# 4. Estado de la revisión cruzada

La documentación fue comparada entre sí.

Resultado:

```text
Domain                    🟢 cerrado
Order / OrderItem         🟢 cerrado
Total / precios           🟢 cerrado
Productos duplicados      🟢 cerrado
Stock / Reservation       🟢 cerrado
ProductCatalog            🟢 cerrado
Consistencia eventual     🟢 cerrado
Customer / ownership     🟢 cerrado
DTOs                      🟢 cerrado
Idempotencia              🟢 cerrado
State Machine             🟢 cerrado
Security                  🟢 cerrado
Event Consumption         🟢 cerrado
Future Architecture       🟢 cerrado
GET /orders + customerId  🟢 cerrado
```

Durante la revisión se detectaron algunos ajustes puramente documentales:

- unificar referencias a `use-case.md`;
- limpiar duplicaciones accidentales en `decisions.md`;
- limpiar duplicaciones accidentales en `future.md`;
- aclarar naming `OrderCreatedEvent` vs `ORDER_CREATED`.

No son cambios de arquitectura.

El comportamiento de:

```text
GET /orders?customerId=...
```

ya quedó definido.

---

# 5. Principios arquitectónicos

## 5.1 Desacoplamiento

Order Service debe permanecer desacoplado de:

- Customer Service;
- Product Service durante Create Order;
- Inventory;
- Payment;
- Pricing / Offer.

El desacoplamiento se logra mediante:

- `ProductCatalog` local;
- eventos;
- snapshots históricos;
- responsabilidades separadas.

---

# 6. Order como Aggregate Root

`Order` es el Aggregate Root.

```text
Order
│
├── id
├── customerId
├── items
│    ├── OrderItem
│    ├── OrderItem
│    └── ...
├── total
├── status
├── createdAt
└── updatedAt
```

`OrderItem` pertenece al Aggregate.

No se manipula independientemente.

El Aggregate protege sus invariantes y controla las transiciones de estado.

---

# 7. Identidad de Order

El ID de `Order`:

```text
- es generado por la base de datos;
- es incremental;
- pertenece a la infraestructura de persistencia;
- no es una regla de negocio del dominio.
```

No generar manualmente el ID desde Application.

No utilizar UUID para cambiar esta decisión.

La estrategia concreta de generación debe ser responsabilidad de PostgreSQL/JPA.

---

# 8. OrderItem como Snapshot Histórico

Cada `OrderItem` conserva:

```text
productId
productName
unitPrice
quantity
subtotal
```

Representa lo que fue comprado en el momento de creación de la Order.

Por lo tanto:

```text
ProductCatalog actual
       ≠
Order histórica
```

Una Order existente no se reconstruye consultando el catálogo actual.

---

# 9. Inmutabilidad comercial

Después de crear una Order, el contenido comercial es inmutable.

No modificar:

```text
customerId
productId
productName
unitPrice
quantity
subtotal
total
```

Los cambios posteriores corresponden al lifecycle y campos técnicos como `updatedAt`.

---

# 10. Cálculo del subtotal y total

`OrderItem` calcula:

```text
subtotal = unitPrice × quantity
```

`Order` calcula:

```text
total = suma de subtotales
```

El flujo es:

```text
OrderItem
   │
   ▼
subtotal
   │
   ▼
Order
   │
   ▼
total
```

El `total`:

- no viene del cliente;
- no se obtiene nuevamente del catálogo;
- es calculado por `Order`;
- se persiste como parte del estado de la Order.

---

# 11. Productos duplicados

El request puede contener:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
```

Application consolida:

```text
2 + 3 = 5
```

Resultado:

```text
OrderItem(
    productId = 10,
    quantity = 5
)
```

La consolidación ocurre:

```text
Request
   │
   ▼
Consolidar duplicados
   │
   ▼
Items normalizados
   │
   ▼
ProductCatalog validation
   │
   ▼
OrderItems
```

Los duplicados:

```text
NO son error
```

La razón principal:

- mantener la invariante de un `productId` único por Order;
- simplificar el cálculo;
- facilitar futuras representaciones como Invoice.

La consolidación pertenece a Application.

El dominio continúa protegiendo la invariante.

---

# 12. ProductCatalog

Order Service tiene:

```text
ProductCatalog
```

como proyección local.

Product Service:

```text
Source of Truth
```

ProductCatalog:

```text
Local Projection
```

Flujo:

```text
Product Service
      │
      │ Product Events
      ▼
   RabbitMQ
      │
      ▼
Order Service Consumer
      │
      ▼
ProductCatalog
```

Eventos actuales:

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_ACTIVATED
PRODUCT_DEACTIVATED
PRODUCT_STOCK_UPDATED
```

La sincronización es eventualmente consistente.

---

# 13. Create Order no llama a Product Service

Durante `POST /orders`:

```text
Order Service
      │
      ▼
ProductCatalog
```

No:

```text
Order Service
      │
      ▼
REST Product Service
```

El desacoplamiento síncrono durante Create Order es una decisión cerrada.

---

# 14. Precio

El cliente NO proporciona:

```text
unitPrice
subtotal
total
```

El precio utilizado proviene de:

```text
ProductCatalog.price
```

Debido a la consistencia eventual, ProductCatalog puede tener temporalmente un precio anterior.

Eso es aceptado en el MVP.

Ejemplo:

```text
Product Service
price = 150

ProductCatalog
price = 100
```

Order Service utiliza el precio conocido:

```text
OrderItem.unitPrice = 100
```

Ese valor queda congelado como snapshot.

No hacer una segunda llamada REST para confirmar el precio.

---

# 15. Pricing / Offer

No implementar Pricing / Offer en el MVP.

Es una evolución futura para necesidades como:

- promociones;
- descuentos;
- cupones;
- precios por Customer;
- reglas comerciales;
- precios temporales;
- pricing más sofisticado.

La arquitectura futura puede evolucionar hacia:

```text
Order
  │
  ▼
Pricing / Offer
```

Pero no implementar ahora.

---

# 16. Stock

Order Service actualmente:

```text
VALIDA
```

el stock conocido.

No:

```text
RESERVA
```

La validación conceptual:

```text
requestedQuantity <= availableStock
```

utilizando `ProductCatalog.availableStock`.

Esto no garantiza ausencia de overselling bajo concurrencia.

Eso es aceptado en el MVP.

---

# 17. Inventory / Reservation

La reserva pertenece a una futura responsabilidad:

```text
Inventory / Reservation
```

Futuras operaciones posibles:

```text
reserve
confirm
release
```

Pero no agregar estados de Inventory al `OrderStatus`.

No implementar reserva dentro de Order Service.

No convertir:

```text
Order created
```

en:

```text
Stock reserved
```

Son responsabilidades diferentes.

---

# 18. Customer y customerId

Order no contiene una entidad Customer completa.

Solo:

```text
customerId
```

`customerId` representa ownership.

No es una entidad `Customer` dentro de Order Service.

No implementar Customer Projection en el MVP.

---

# 19. Actor ≠ Customer

El actor autenticado representa:

```text
quién ejecuta
```

`customerId` representa:

```text
para quién se crea
```

Por lo tanto:

```text
Actor ≠ Customer
```

Ejemplo:

```text
ADMIN
actorId = 1
customerId = 25
```

La Order pertenece al customer 25 aunque el actor sea el administrador 1.

---

# 20. USER

Para USER:

```text
customerId = authenticatedCustomerId
```

El USER:

- crea sus propias Orders;
- consulta sus propias Orders;
- lista sus propias Orders;
- cancela sus propias Orders elegibles.

No puede crear/consultar Orders de otro customer.

---

# 21. ADMIN

ADMIN puede:

- crear Order para un customer;
- consultar Orders;
- listar Orders;
- filtrar por customer;
- cancelar Orders elegibles.

ADMIN no se convierte en owner de la Order.

---

# 22. GET /orders

Contrato:

```http
GET /orders
```

Respuesta:

```text
Page<OrderSummaryResponse>
```

`OrderSummaryResponse` NO contiene items.

Conceptualmente:

```text
OrderSummaryResponse
├── id
├── customerId
├── total
├── status
├── createdAt
└── updatedAt
```

---

# 23. Filtro customerId

Contrato:

```http
GET /orders?customerId=25
```

Reglas:

```text
USER
  ↓
solo sus Orders
```

El USER no puede utilizar `customerId` para acceder a Orders de otro customer.

```text
ADMIN
  ↓
puede utilizar customerId como filtro
```

Por lo tanto:

```text
security-authorization.md
        =
use-case.md
        =
api.md
```

respecto a este comportamiento.

---

# 24. GET /orders/{id}

Contrato:

```http
GET /orders/{id}
```

Respuesta:

```text
OrderResponse
```

Incluye:

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

El detalle se obtiene del snapshot persistido.

No consultar Product Service para reconstruirlo.

---

# 25. CreateOrderRequest

Existe un único DTO HTTP para USER y ADMIN:

```text
CreateOrderRequest
├── customerId?
└── items[]
     └── CreateOrderItemRequest
         ├── productId
         └── quantity
```

No crear:

```text
CreateOrderRequestUser
CreateOrderRequestAdmin
```

La diferencia USER/ADMIN se resuelve en Application / Authorization / Ownership.

---

# 26. CreateOrderCommand

HTTP:

```text
CreateOrderRequest
```

Application:

```text
CreateOrderCommand
├── customerId
├── items
└── actor
```

No mezclar DTO HTTP con objetos de dominio.

El dominio no conoce:

- JWT;
- Spring Security;
- SecurityContext;
- Idempotency-Key.

---

# 27. Idempotencia

`POST /orders` requiere obligatoriamente:

```http
Idempotency-Key: <unique-key>
```

Esto forma parte del MVP.

---

# 28. Misma Idempotency-Key + mismo request

Si llega:

```text
same Idempotency-Key
+
same request
```

es el mismo intento lógico.

No crear otra Order.

Devolver el resultado original.

---

# 29. Misma Idempotency-Key + request diferente

Si llega:

```text
same Idempotency-Key
+
different request
```

rechazar la operación.

Conceptualmente:

```text
Idempotency-Key
        +
Request fingerprint
```

permite distinguir:

```text
same key + same request
        ↓
resultado original

same key + different request
        ↓
error
```

---

# 30. Persistencia de Idempotencia

PostgreSQL conceptualmente contiene:

```text
orders
order_items
idempotency_records
```

Create Order debe mantener consistencia transaccional:

```text
BEGIN

    create Order
    create OrderItems
    create IdempotencyRecord

COMMIT
```

Si falla:

```text
ROLLBACK
```

No debe quedar:

```text
Order creada
+
sin registro de idempotencia
```

ni:

```text
IdempotencyRecord
+
sin Order
```

La implementación concreta de `IdempotencyRecord` debe seguir la documentación ya definida.

No utilizar Redis/Caffeine solo para resolver esta garantía.

La garantía debe sobrevivir reinicios del servicio.

---

# 31. State Machine

Estado inicial:

```text
PENDING_PAYMENT
```

Transiciones:

```text
PENDING_PAYMENT
       │
       ├── pay() ──► PAID
       │
       └── cancel() ► CANCELLED
```

Estados terminales:

```text
PAID
CANCELLED
```

No permitir:

```text
PAID → CANCELLED
PAID → PENDING_PAYMENT
CANCELLED → PAID
CANCELLED → PENDING_PAYMENT
```

No usar setter arbitrario:

```java
order.setStatus(...)
```

Las transiciones deben estar protegidas por comportamiento del Aggregate:

```text
order.pay()
order.cancel()
```

---

# 32. Cancel Order

Endpoint definido:

```http
PATCH /orders/{id}/cancel
```

La transición válida:

```text
PENDING_PAYMENT
       │
       ▼
CANCELLED
```

El endpoint no recibe un estado arbitrario.

Application invoca el comportamiento del Aggregate.

---

# 33. No eliminar Orders

Las Orders no se eliminan físicamente como parte del lifecycle.

El estado representa su evolución.

Esto permite conservar:

- historial;
- auditoría;
- trazabilidad;
- métricas;
- análisis.

---

# 34. Errores de negocio

Errores relevantes:

```text
OrderNotFound
OrderNotCancellable
ProductNotFound
ProductInactive
InsufficientStock
InvalidOrderItem
UnauthorizedOrderAccess
```

También errores relacionados con:

```text
Idempotency-Key
```

Los duplicados de productos:

```text
NO son error
```

porque se consolidan.

Separar:

```text
Business Error
```

de:

```text
Infrastructure Error
```

---

# 35. Seguridad

Separación:

```text
Authentication
      ↓
Authorization
      ↓
Ownership
      ↓
Application Use Case
      ↓
Domain Rules
```

Authentication determina quién es.

Authorization determina qué puede intentar.

Ownership determina sobre qué recurso.

Domain determina si la operación es válida.

El dominio no debe conocer:

- Spring Security;
- JWT;
- SecurityContext;
- roles;
- mecanismos de autenticación.

---

# 36. Responsabilidades de Application

Application coordina:

```text
Application
│
├── Actor
├── Authorization
├── Ownership
├── Idempotency-Key
├── normalización
├── consolidación de items
├── ProductCatalog
├── creación del Aggregate
└── persistencia
```

Application NO debe absorber reglas que pertenecen al dominio.

---

# 37. Responsabilidades del Domain

Domain:

```text
Order
├── invariantes
├── OrderItems
├── subtotal
├── total
└── lifecycle

OrderItem
└── subtotal
```

Domain no:

- consulta ProductCatalog;
- consulta Product Service;
- conoce JWT;
- conoce Spring Security;
- conoce Idempotency-Key;
- coordina persistencia;
- reserva stock.

---

# 38. Flujo completo de Create Order

Flujo definitivo:

```text
Client
  │
  ▼
POST /orders
  │
  ▼
Idempotency-Key
  │
  ▼
Authentication / Authorization
  │
  ▼
Resolver customerId
  │
  ▼
Consolidar productos duplicados
  │
  ▼
ProductCatalog
  │
  ├── exists?
  ├── ACTIVE?
  └── availableStock sufficient?
  │
  ▼
Build OrderItems
  │
  ├── productId
  ├── productName
  ├── unitPrice
  ├── quantity
  └── subtotal
  │
  ▼
Build Order
  │
  ├── customerId
  ├── items
  ├── total
  └── PENDING_PAYMENT
  │
  ▼
Persistence Transaction
  │
  ├── Order
  ├── OrderItems
  └── IdempotencyRecord
  │
  ▼
COMMIT
  │
  ▼
HTTP 201
  │
  ▼
OrderResponse
```

---

# 39. Flujo ante retry

```text
Client
   │
   ├── Request 1
   │
   ▼
Order Service
   │
   ▼
Order created
   │
   ▼
Response lost
   │
   │ retry
   ▼
Same Idempotency-Key
   │
   ▼
IdempotencyRecord
   │
   ▼
Return original result
```

No crear segunda Order.

---

# 40. Product Event Consumption

El Consumer mantiene ProductCatalog.

```text
RabbitMQ
   │
   ▼
ProductCatalogConsumer
   │
   ▼
ProductCatalogService
   │
   ▼
ProductCatalogRepository
```

El Consumer no:

- crea Orders;
- modifica Orders;
- calcula Order totals;
- reserva stock;
- llama Product Service REST.

---

# 41. Eventos actuales de Product

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_ACTIVATED
PRODUCT_DEACTIVATED
PRODUCT_STOCK_UPDATED
```

Actualizan ProductCatalog.

---

# 42. Event naming futuro

Cuando Order publique eventos en el futuro, mantener separación entre:

```text
Class:
OrderCreatedEvent
```

y:

```text
eventType:
ORDER_CREATED
```

Es decir:

```text
OrderCreatedEvent
        │
        └── eventType = ORDER_CREATED
```

No tratar estas dos nomenclaturas como conceptos diferentes.

---

# 43. Eventos de Order

Actualmente:

```text
NO publicar eventos propios de Order como requisito del MVP actual.
```

Es una evolución futura.

---

# 44. Futuras capacidades

No implementar todavía:

```text
Payment
Inventory / Reservation
Pricing / Offer
Order Events
Retry
Consumer Retry
DLQ
Publisher Confirms
Consumer Idempotency
Outbox Pattern
Saga Pattern
Customer Projection
Idempotencia end-to-end
Observabilidad distribuida avanzada
```

Estas capacidades se incorporarán cuando exista una necesidad real.

---

# 45. HTTP Idempotency vs Consumer Idempotency

Son problemas diferentes.

## MVP

```text
HTTP Idempotency
POST /orders
       ↓
Idempotency-Key
       ↓
evitar Orders duplicadas
```

## Futuro

```text
Consumer Idempotency
Event / Message
       ↓
evitar efectos duplicados por redelivery
```

No confundir ambos mecanismos.

---

# 46. Pricing / Offer futuro

No implementar ahora.

Puede resolver posteriormente:

- promociones;
- descuentos;
- cupones;
- pricing por customer;
- reglas comerciales;
- precios temporales.

---

# 47. Customer Projection futura

No implementar Customer Projection prematuramente.

Actualmente basta:

```text
Order.customerId
```

Si aparece una necesidad concreta de consultar información de Customer localmente, se podrá evaluar una proyección futura.

---

# 48. Payment futuro

Payment es una responsabilidad independiente.

No convertir:

```text
Order
```

en:

```text
Payment
```

ni meter reglas de pago dentro del Aggregate.

Estado actual:

```text
PENDING_PAYMENT
```

Payment será una evolución futura.

---

# 49. Inventory futuro

Inventory será responsable de reserva y coordinación de stock.

No agregar:

```text
reservedStock
```

al Aggregate Order.

No mezclar:

```text
Order lifecycle
```

con:

```text
Inventory lifecycle
```

---

# 50. Retry / DLQ / Publisher Confirms / Outbox / Saga

Son patrones futuros.

No implementarlos anticipadamente durante la primera implementación de Create Order salvo que el roadmap de implementación indique específicamente una fase correspondiente.

El objetivo es evitar sobreingeniería.

---

# 51. Roadmap de implementación

Estado actual:

```text
FASE 1 — Diseño del dominio
             ✅

FASE 2 — Application / Security
             ✅

FASE 3 — API
             ✅

FASE 4 — Implementación Create Order
             ← SIGUIENTE

FASE 5 — Consultas
FASE 6 — Lifecycle
FASE 7 — Order Events
FASE 8 — Notification
FASE 9 — Payment
FASE 10 — Inventory / Reservation
FASE 11 — Resiliencia de mensajería
FASE 12 — Consistencia avanzada
```

---

# 52. Fase actual: Implementación Create Order

Pendiente:

```text
[ ] Implementar Order
[ ] Implementar OrderItem
[ ] Implementar OrderStatus
[ ] Implementar invariantes
[ ] Implementar comportamiento del Aggregate
[ ] Implementar Create Order Use Case
[ ] Integrar ProductCatalog
[ ] Implementar persistencia Order + OrderItems
[ ] Implementar IdempotencyRecord
[ ] Implementar transacción
[ ] Implementar consolidación de duplicados
[ ] Implementar DTOs
[ ] Implementar Controller
[ ] Implementar manejo de errores
[ ] Tests unitarios de dominio
[ ] Tests del caso de uso
[ ] Tests de integración
[ ] Test retry misma Idempotency-Key
[ ] Test misma key + request diferente
```

---

# 53. Orden recomendado de implementación

```text
1. Domain
   │
   ├── Order
   ├── OrderItem
   ├── OrderStatus
   └── invariants
   │
   ▼
2. Persistence
   │
   ├── Order entity
   ├── OrderItem entity
   └── repositories
   │
   ▼
3. Application
   │
   └── CreateOrderUseCase
   │
   ▼
4. ProductCatalog integration
   │
   ▼
5. Item normalization
   │
   └── consolidate duplicate productId
   │
   ▼
6. Pricing snapshot
   │
   ▼
7. Order total
   │
   ▼
8. Idempotency
   │
   ├── IdempotencyRecord
   ├── request fingerprint
   └── transactional persistence
   │
   ▼
9. API
   │
   ├── CreateOrderRequest
   ├── OrderResponse
   └── Controller
   │
   ▼
10. Error handling
   │
   ▼
11. Tests
```

El orden puede ajustarse técnicamente si la implementación lo requiere, pero no cambiar las decisiones del diseño.

---

# 54. Stack técnico relevante

El proyecto utiliza:

```text
Java 21
Spring Boot 3
Spring Security
Spring Data JPA
PostgreSQL
RabbitMQ
Maven
Docker
JUnit 5
Mockito
Log4j2
```

Order Service utiliza PostgreSQL como base de datos propia.

RabbitMQ se utiliza para eventos de sincronización del ProductCatalog.

---

# 55. Base de datos

Order Service tiene su propia base:

```text
ecommerce_order_db
```

Conceptualmente:

```text
orders
order_items
idempotency_records
product_catalog
```

`product_catalog` representa la proyección local.

No compartir tablas de Product Service.

---

# 56. Transaction Boundary

Para Create Order:

```text
@Transactional
```

debe cubrir conceptualmente:

```text
Order
+
OrderItems
+
IdempotencyRecord
```

El ProductCatalog es leído, no escrito dentro de esta transacción.

Si falla la creación:

```text
ROLLBACK
```

---

# 57. No introducir cambios innecesarios

Durante la implementación:

NO:

- cambiar `PATCH /orders/{id}/cancel` a POST;
- cambiar Order ID a UUID;
- introducir Customer entity;
- introducir Customer Projection;
- introducir Inventory dentro de Order;
- introducir Pricing Service;
- hacer REST a Product Service durante Create Order;
- hacer que el cliente envíe precio;
- permitir modificar una Order histórica;
- convertir duplicados en error;
- hacer que Order reserve stock;
- mover reglas de dominio a Controller;
- meter Spring Security dentro del dominio;
- implementar Outbox/Saga prematuramente.

---

# 58. Criterio de implementación

La implementación debe respetar esta separación:

```text
Controller
   │
   ▼
Application
   │
   ├── Authorization / Ownership
   ├── Idempotency
   ├── normalization
   ├── ProductCatalog
   └── transaction coordination
   │
   ▼
Domain
   │
   ├── Order
   ├── OrderItem
   ├── invariants
   ├── calculations
   └── state transitions
   │
   ▼
Persistence
```

---

# 59. Regla para dudas durante implementación

Si aparece una duda:

1. revisar primero `decisions.md`;
2. revisar `domain.md`;
3. revisar `use-case.md`;
4. revisar `api.md`;
5. revisar `order-flow.md`;
6. revisar `security-authorization.md`;
7. revisar `product-catalog.md`;
8. revisar `roadmap.md` / `future.md`.

No resolver una duda de implementación contradiciendo una decisión cerrada sin identificar primero una contradicción real.

---

# 60. Criterio de éxito del primer incremento

El primer incremento funcional debe permitir:

```text
POST /orders
        │
        ▼
authenticated actor
        │
        ▼
resolve customerId
        │
        ▼
consolidate duplicate products
        │
        ▼
validate ProductCatalog
        │
        ▼
create Order
        │
        ▼
calculate subtotal
        │
        ▼
calculate total
        │
        ▼
persist Order + Items + IdempotencyRecord
        │
        ▼
201 Created
        │
        ▼
OrderResponse
```

Y debe soportar:

```text
retry same Idempotency-Key
```

sin crear una segunda Order.

---

# 61. Tests mínimos esperados

## Domain

- crea Order válida;
- rechaza Order sin items;
- rechaza cantidad inválida;
- rechaza productId duplicado dentro del Aggregate;
- calcula subtotal;
- calcula total;
- inicia en PENDING_PAYMENT;
- `pay()` desde PENDING_PAYMENT → PAID;
- `cancel()` desde PENDING_PAYMENT → CANCELLED;
- impide transiciones inválidas;
- mantiene contenido comercial inmutable.

## Application

- USER obtiene customerId del actor;
- ADMIN puede crear para customer;
- USER no puede crear para otro customer;
- consolida duplicados;
- consulta ProductCatalog;
- rechaza producto inexistente;
- rechaza producto INACTIVE;
- rechaza stock conocido insuficiente;
- usa precio de ProductCatalog;
- construye snapshot;
- persiste total.

## Idempotency

- primera request crea Order;
- retry con misma key + mismo request devuelve resultado original;
- misma key + request diferente produce error;
- fallo transaccional no deja Order parcial;
- fallo transaccional no deja IdempotencyRecord huérfano.

## API

- `POST /orders` devuelve 201;
- `GET /orders/{id}` devuelve detalle;
- `GET /orders` devuelve summary paginado;
- USER solo accede a sus Orders;
- ADMIN puede filtrar por customerId;
- cancelación respeta State Machine.

---

# 62. Documentación después de implementar

Cuando una decisión de implementación confirme el diseño:

```text
código
  ↓
tests
  ↓
documentación
```

Si una implementación revela una contradicción real:

```text
código / restricción técnica
       ↓
identificar contradicción
       ↓
evaluar decisión
       ↓
actualizar decisions.md
       ↓
actualizar documentos afectados
       ↓
implementar
```

No modificar silenciosamente la documentación para que coincida con una implementación que contradice una decisión.

---

# 63. Resumen ejecutivo

La arquitectura final de Order Service es:

```text
                    ┌─────────────────────┐
                    │    Product Service   │
                    │   Source of Truth    │
                    └──────────┬──────────┘
                               │
                         Product Events
                               │
                               ▼
                          RabbitMQ
                               │
                               ▼
                    ┌─────────────────────┐
                    │    ProductCatalog   │
                    │   Local Projection   │
                    └──────────┬──────────┘
                               │
                               ▼
                        Create Order
                               │
                ┌──────────────┼──────────────┐
                ▼              ▼              ▼
           Idempotency    Ownership      Normalize Items
                │              │              │
                └──────────────┼──────────────┘
                               ▼
                         ProductCatalog
                               │
                               ▼
                            Order
                         ┌─────┴─────┐
                         │           │
                    OrderItems     total
                         │
                         ▼
                    PostgreSQL
                         │
                ┌────────┼─────────┐
                ▼        ▼         ▼
              orders  order_items  idempotency_records
```

El principio fundamental es:

```text
Order Service conoce lo necesario para crear
y gestionar una Order,

pero no se convierte en dueño de:

Customer
Product
Inventory
Payment
Pricing
```

---

# 64. Punto exacto donde comienza el nuevo chat

El nuevo chat debe comenzar desde:

```text
DISEÑO CERRADO
      │
      ▼
IMPLEMENTACIÓN DE ORDER SERVICE
      │
      ▼
FASE 4 — CREATE ORDER
```

Primera tarea:

```text
Revisar la estructura actual del order-service
y comenzar la implementación de Domain:
Order
OrderItem
OrderStatus
invariantes
comportamiento del Aggregate
```

No comenzar modificando todos los servicios.

No rehacer documentación.

No volver a discutir las decisiones arquitectónicas cerradas.

Primero aterrizar el diseño en código de forma incremental y testeable.

---

# 65. Frase de inicio para el nuevo chat

Copiar al comenzar el nuevo chat:

> **Continuemos la implementación de Order Service desde este contexto. El diseño ya está cerrado y documentado. No vuelvas a revisar las decisiones arquitectónicas cerradas. Empecemos por la Fase 4 — Implementación de Create Order, comenzando por Domain (`Order`, `OrderItem`, `OrderStatus` e invariantes). Avancemos archivo por archivo, indicando package, clase, responsabilidad y código necesario, y mantengamos el diseño exactamente como está definido en este contexto.**
