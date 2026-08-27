# Order Service --- Contexto Maestro de Continuación

> Documento para iniciar un nuevo chat y continuar exactamente desde el
> punto actual del diseño de `order-service`.
>
> **Fecha de corte:** 2026-08-17
>
> **Regla principal:** no volver a revisar decisiones ya cerradas.
> Continuar desde la revisión cruzada de los 12 documentos.

------------------------------------------------------------------------

# 1. Objetivo de esta continuación

Estamos rediseñando `order-service` desde el dominio hacia la
implementación.

El objetivo es cerrar primero el diseño y recién después implementar.

La secuencia acordada originalmente era:

``` text
CONTEXTO YA CERRADO
        │
        ▼
Revisión de puntos pendientes
        │
        ├── 1. Contrato definitivo de Order
        ├── 2. Contrato definitivo de OrderItem
        ├── 3. Productos duplicados
        ├── 4. Stock + concurrencia
        ├── 5. Precio + consistencia eventual
        ├── 6. Customer / customerId
        ├── 7. DTOs definitivos
        ├── 8. Errores de negocio
        ├── 9. Idempotencia
        └── 10. State Machine completa
        │
        ▼
Revisión cruzada de los 12 documentos
        │
        ▼
DISEÑO CERRADO
        │
        ▼
IMPLEMENTACIÓN
```

Los puntos 1--10 ya fueron revisados y cerrados en esta continuación.

**El siguiente trabajo NO es volver a discutir esos puntos.**

El siguiente paso es:

> Revisar los 12 documentos actuales de `order-service` y comprobar que
> sean coherentes con todas las decisiones cerradas.

------------------------------------------------------------------------

# 2. Arquitectura general ya cerrada

Proyecto:

``` text
Ecommerce Microservices Demo
```

Stack:

``` text
Java 21
Spring Boot 3
Spring Security
JWT
RabbitMQ
PostgreSQL
Spring Data JPA
Docker
Maven
Log4j2
```

Servicios:

``` text
api-gateway
common-lib
product-service
order-service
notification-service
```

Evoluciones futuras:

``` text
Payment Service
Inventory / Reservation
Pricing / Offer
Customer Projection
Notification
```

No todos forman parte del MVP de `order-service`.

------------------------------------------------------------------------

# 3. Arquitectura central de Order Service

`Product Service` es el dueño del catálogo:

``` text
Product Service
    └── Source of Truth
```

`Order Service` mantiene una proyección local:

``` text
ProductCatalog
    └── proyección local del catálogo
```

Flujo:

``` text
Product Service
      │
      │ Product Events
      ▼
   RabbitMQ
      │
      ▼
Order Service
      │
      ▼
ProductCatalog
```

Order Service NO consulta Product Service mediante REST durante
`Create Order`.

Motivos:

-   desacoplamiento;
-   menor latencia;
-   menor dependencia síncrona;
-   disponibilidad;
-   evolución hacia arquitectura event-driven.

`ProductCatalog` no es un segundo Product Service.

------------------------------------------------------------------------

# 4. Actor, Authorization, Ownership y Domain

Separación definitiva:

``` text
Authentication
      ↓
¿Quién es el actor?

Authorization
      ↓
¿Puede intentar ejecutar la operación?

Ownership
      ↓
¿Sobre qué Customer / Order puede operar?

Domain Rules
      ↓
¿La operación es válida según el negocio?
```

`Order` no conoce:

``` text
JWT
SecurityContext
Spring Security
roles como detalle de infraestructura
```

Actor y Customer son conceptos diferentes:

``` text
Actor
    └── quién ejecuta

customerId
    └── propietario de la Order
```

Ejemplo:

``` text
USER
actorId    = 25
customerId = 25
```

pero:

``` text
ADMIN
actorId    = 1
customerId = 25
```

------------------------------------------------------------------------

# 5. Order --- contrato definitivo

`Order` es el Aggregate Root.

Contrato:

``` text
Order
├── id
├── customerId
├── items
├── total
├── status
├── createdAt
└── updatedAt
```

## ID

`Order.id`:

``` text
DB generated
incremental
```

No UUID generado por la aplicación.

------------------------------------------------------------------------

## customerId

`customerId` representa ownership.

No es una entidad `Customer` dentro de Order Service.

Order solo necesita:

``` text
customerId
```

No se crea Customer Projection en el MVP.

------------------------------------------------------------------------

## items

`items` pertenece al Aggregate.

`OrderItem` no se manipula independientemente.

------------------------------------------------------------------------

## total

Decisión definitiva:

> **Order calcula el total y el total se persiste.**

Flujo:

``` text
OrderItem
    ↓
subtotal = unitPrice × quantity
    ↓
Order
    ↓
total = suma de subtotales
    ↓
persistir total
```

`total`:

-   no viene del cliente;
-   no lo calcula el controller;
-   no lo calcula el repository;
-   no se obtiene nuevamente del catálogo;
-   no puede modificarse arbitrariamente.

Es estado protegido por el Aggregate.

------------------------------------------------------------------------

## createdAt / updatedAt

`createdAt`:

-   representa creación;
-   es inmutable.

`updatedAt`:

-   representa el último cambio relevante;
-   cambia ante modificaciones permitidas;
-   en el MVP principalmente cuando cambia el estado.

------------------------------------------------------------------------

# 6. OrderItem --- contrato definitivo

Contrato:

``` text
OrderItem
├── productId
├── productName
├── unitPrice
├── quantity
└── subtotal
```

Invariantes:

``` text
quantity > 0
unitPrice > 0
subtotal = unitPrice × quantity
```

`productName` es snapshot histórico.

`unitPrice` es snapshot histórico.

`OrderItem` pertenece exclusivamente a `Order`.

Una Order existente no depende del catálogo actual para reconstruir la
compra.

------------------------------------------------------------------------

# 7. Productos duplicados --- decisión definitiva

Los productos duplicados en el request NO son un error.

Se consolidan dentro de `Order`.

Ejemplo:

``` text
productId = 10, quantity = 2
productId = 10, quantity = 3
```

se convierte en:

``` text
productId = 10, quantity = 5
```

La Order termina con una sola línea:

``` text
Order
└── OrderItem
      ├── productId = 10
      └── quantity  = 5
```

Motivo importante:

> Tener una única línea por producto simplifica posteriormente el módulo
> de Invoice.

Invoice no debería tener que volver a normalizar o agrupar líneas.

Después de consolidar:

``` text
quantity
   ↓
OrderItem
   ↓
subtotal
   ↓
Order.total
```

La invariante sigue siendo:

> Un `productId` no puede aparecer más de una vez dentro de una Order.

------------------------------------------------------------------------

# 8. Stock + concurrencia --- decisión definitiva

Order Service valida:

``` text
requestedQuantity <= ProductCatalog.availableStock
```

También valida:

``` text
producto existe
producto ACTIVE
```

Pero Order Service NO reserva stock.

No hace:

``` text
decrementar stock
```

ni:

``` text
reservar stock
```

ni introduce una solución de concurrencia de inventario.

Porque:

``` text
stock observado
    ≠
stock reservado
```

Puede existir el escenario:

``` text
Stock conocido = 1

Order A → observa 1
Order B → observa 1
```

Ambas pueden pasar la validación en el MVP.

Esto es una limitación consciente.

La reserva y la concurrencia real quedan para:

``` text
Inventory / Reservation
```

Futuras operaciones:

``` text
reserve
confirm
release
```

------------------------------------------------------------------------

# 9. Precio + consistencia eventual --- decisión definitiva

Order utiliza:

``` text
ProductCatalog.price
```

como precio conocido localmente.

Ejemplo:

``` text
ProductCatalog
price = 100
```

Order construye:

``` text
OrderItem
unitPrice = 100
```

y luego:

``` text
Order.total
```

El precio queda congelado como snapshot histórico.

Puede existir:

``` text
Product Service
price = 150

ProductCatalog
price = 100
```

mientras el evento está pendiente.

En el MVP:

> Se acepta la consistencia eventual del precio.

No se agrega:

``` text
Order → REST → Product Service
```

para corregirlo.

Esto mantiene el desacoplamiento.

Si en el futuro el negocio necesita garantías de pricing más
sofisticadas:

``` text
Pricing / Offer Service
```

puede convertirse en una capability futura.

No implementar Pricing ahora.

------------------------------------------------------------------------

# 10. Customer / customerId --- decisión definitiva

Order Service solo necesita:

``` text
customerId
```

No necesita:

``` text
Customer
name
email
address
phone
```

## USER

El `customerId` confiable viene del actor autenticado:

``` text
Actor
  ↓
customerId
  ↓
Order.customerId
```

USER no puede elegir arbitrariamente otro customerId.

## ADMIN

ADMIN puede especificar:

``` text
customerId
```

cuando la autorización lo permite.

Por lo tanto existe un único concepto de `Create Order`, no dos dominios
distintos.

No existe Customer Projection en el MVP.

No se hace llamada síncrona a Customer Service.

Customer Projection es una evolución futura.

------------------------------------------------------------------------

# 11. DTOs --- contrato definitivo

## CreateOrderRequest

Un único DTO para USER y ADMIN:

``` text
CreateOrderRequest
├── customerId?
└── items[]
    └── CreateOrderItemRequest
        ├── productId
        └── quantity
```

La diferencia USER/ADMIN se resuelve mediante Authorization/Ownership.

USER:

``` text
customerId = actor.customerId
```

ADMIN:

``` text
customerId = request.customerId
```

El request NO acepta como valores confiables:

``` text
price
unitPrice
subtotal
total
status
createdAt
updatedAt
```

------------------------------------------------------------------------

## OrderResponse

``` text
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

------------------------------------------------------------------------

## OrderSummaryResponse

Para:

``` text
GET /orders
```

se decidió utilizar una respuesta resumida:

``` text
OrderSummaryResponse
├── id
├── customerId
├── total
├── status
├── createdAt
└── updatedAt
```

No incluye `items`.

Por lo tanto:

``` text
GET /orders/{id}
    ↓
OrderResponse
```

y:

``` text
GET /orders
    ↓
Page<OrderSummaryResponse>
```

------------------------------------------------------------------------

# 12. API conceptual definitiva

``` text
POST /orders
    Request  → CreateOrderRequest
    Response → OrderResponse

GET /orders/{id}
    Response → OrderResponse

GET /orders
    Response → Page<OrderSummaryResponse>

POST /orders/{id}/cancel
    Response → OrderResponse
```

Cancelación no recibe:

``` json
{
  "status": "CANCELLED"
}
```

El cliente no decide directamente la transición.

El Aggregate ejecuta:

``` text
Order.cancel()
```

------------------------------------------------------------------------

# 13. Errores de negocio --- decisión definitiva

Separamos errores de negocio de errores de infraestructura.

## Casos de negocio

``` text
ProductNotFound
ProductInactive
InsufficientStock
OrderNotFound
OrderNotCancellable
UnauthorizedOrderAccess
InvalidOrderItem
```

Conceptualmente:

### Producto inexistente

``` text
ProductCatalog
    ↓
no existe
    ↓
ERROR
    ↓
NO crear Order
```

### Producto INACTIVE

``` text
exists
+
INACTIVE
    ↓
ERROR
```

### Stock insuficiente

``` text
requestedQuantity > availableStock
    ↓
ERROR
```

### Quantity inválida

``` text
quantity <= 0
    ↓
ERROR
```

### Order sin items

``` text
items = []
    ↓
ERROR
```

### Order ajena

Es problema de:

``` text
Authorization / Ownership
```

no del Aggregate.

### Order inexistente

Es un recurso no encontrado.

### Cancelación inválida

Ejemplo:

``` text
PAID → CANCELLED
```

es error de dominio.

------------------------------------------------------------------------

## Duplicados

No son error:

``` text
2 + 3 = 5
```

Se consolidan.

------------------------------------------------------------------------

## Infraestructura

No confundir:

``` text
Database failure
RabbitMQ failure
```

con errores de negocio.

La persistencia de:

``` text
Order + OrderItems
```

debe ser transaccional:

``` text
BEGIN
    save Order
    save OrderItems

    OK → COMMIT
    ERROR → ROLLBACK
```

------------------------------------------------------------------------

# 14. Consistencia eventual y errores

Se acepta que `ProductCatalog` puede estar temporalmente desactualizado.

Ejemplo:

``` text
Product Service = ACTIVE
ProductCatalog  = INACTIVE
```

Order Service utilizará lo que conoce localmente.

Por lo tanto:

``` text
ProductCatalog INACTIVE
    ↓
Order rechaza
```

No se hace una llamada síncrona a Product Service para corregir el
estado.

Esto es consecuencia consciente del desacoplamiento elegido.

------------------------------------------------------------------------

# 15. Idempotencia --- decisión definitiva

`POST /orders` requiere obligatoriamente:

``` http
Idempotency-Key: <unique-key>
```

Motivo:

``` text
cliente
  ↓
POST /orders
  ↓
Order creada
  ↓
respuesta perdida
  ↓
retry
```

Sin idempotencia:

``` text
Order 100
Order 101
```

Con idempotencia:

``` text
mismo Idempotency-Key
    ↓
misma operación
    ↓
misma Order
```

------------------------------------------------------------------------

## Persistencia

Se tendrá conceptualmente:

``` text
PostgreSQL
├── orders
├── order_items
└── idempotency_records
```

`IdempotencyRecord` puede contener conceptualmente:

``` text
key
requestHash / fingerprint
orderId
status
createdAt
```

La creación debe ser atómica:

``` text
BEGIN

    crear Order
    crear OrderItems
    guardar IdempotencyRecord

COMMIT
```

------------------------------------------------------------------------

## Reutilización incorrecta de key

Misma key + mismo request:

``` text
→ devolver resultado original
```

Misma key + request diferente:

``` text
→ error
```

No se introduce Redis/Caffeine para esto en el MVP.

PostgreSQL es suficiente.

------------------------------------------------------------------------

## Alcance

La obligatoriedad aplica principalmente a:

``` text
POST /orders
```

No se introduce ahora el mismo mecanismo para todos los endpoints.

------------------------------------------------------------------------

# 16. Retry futuro vs Idempotency

No mezclar:

### Idempotencia HTTP

Resuelve:

``` text
cliente reintenta POST /orders
```

### Patrones futuros de mensajería

Ya estaban previstos:

``` text
Retry
DLQ
Publisher Confirms
Consumidores idempotentes
Outbox Pattern
```

Estos resuelven problemas distintos de comunicación/eventos.

No se implementan anticipadamente.

------------------------------------------------------------------------

# 17. State Machine --- definitiva

Estados MVP:

``` text
PENDING_PAYMENT
PAID
CANCELLED
```

Estado inicial:

``` text
PENDING_PAYMENT
```

Transiciones:

``` text
PENDING_PAYMENT
    │
    ├── pay()    → PAID
    │
    └── cancel() → CANCELLED
```

Estados terminales:

``` text
PAID
CANCELLED
```

No:

``` text
PAID → CANCELLED
CANCELLED → PAID
CANCELLED → PENDING_PAYMENT
```

El estado no se modifica mediante setter arbitrario.

Debe utilizarse comportamiento del Aggregate:

``` text
order.pay()
order.cancel()
```

------------------------------------------------------------------------

# 18. Payment futuro

`PAID` representa confirmación real de pago.

No:

``` text
ADMIN → setStatus(PAID)
```

Futuro:

``` text
Payment Service
      ↓
PaymentConfirmedEvent
      ↓
Order Service
      ↓
order.pay()
      ↓
PAID
```

------------------------------------------------------------------------

# 19. Refund futuro

No agregar `REFUNDED` ni `REFUND_REQUESTED` al MVP.

Futuro posible:

``` text
PAID
  ↓
REFUND_REQUESTED
  ↓
REFUNDED
```

No mezclar cancelación pendiente con refund de una Order pagada.

------------------------------------------------------------------------

# 20. Inventory futuro

No agregar estados de inventario a `OrderStatus`.

Inventory tendrá su propio lifecycle:

``` text
reserve
confirm
release
```

La coordinación futura puede evolucionar hacia Saga.

No implementar Saga ahora.

------------------------------------------------------------------------

# 21. Flujo Create Order definitivo

``` text
POST /orders
      ↓
Validar Request
      ↓
Validar Idempotency-Key
      ↓
Identificar Actor
      ↓
Resolver customerId
      ↓
Consultar ProductCatalog
      ↓
Validar producto existente
      ↓
Validar producto ACTIVE
      ↓
Validar stock conocido
      ↓
Consolidar productos duplicados
      ↓
Construir OrderItems
      ↓
Calcular subtotales
      ↓
Calcular total
      ↓
Construir Order
      ↓
PENDING_PAYMENT
      ↓
Persistir:
    Order
    OrderItems
    IdempotencyRecord
      ↓
COMMIT
      ↓
OrderResponse
```

------------------------------------------------------------------------

# 22. Documentación existente

La estructura documental acordada es exactamente:

``` text
order-service/
├── README.md
├── domain.md
├── use-cases.md
├── security-authorization.md
├── api.md
├── order-flow.md
├── product-catalog.md
├── synchronization.md
├── event-consumption.md
├── decisions.md
├── roadmap.md
└── future.md
```

Responsabilidad:

``` text
README.md
→ fotografía general

domain.md
→ Order, OrderItem, Aggregate, invariantes, comportamiento y estados

use-cases.md
→ casos de uso

security-authorization.md
→ actor, authentication, authorization, ownership

api.md
→ HTTP y DTOs

order-flow.md
→ flujos funcionales

product-catalog.md
→ ProductCatalog y responsabilidad

synchronization.md
→ sincronización y consistencia eventual

event-consumption.md
→ consumo y procesamiento de eventos

decisions.md
→ decisiones y justificaciones

roadmap.md
→ evolución y estado

future.md
→ visión futura
```

Regla:

> Cada documento responde una pregunta diferente y no documenta todo el
> sistema.

------------------------------------------------------------------------

# 23. Próximo paso exacto

**NO implementar todavía.**

Primero debemos hacer:

``` text
REVISIÓN CRUZADA DE LOS 12 DOCUMENTOS
```

Objetivos:

``` text
1. Detectar contradicciones.
2. Detectar decisiones que quedaron fuera de los documentos.
3. Detectar nombres diferentes para el mismo concepto.
4. Detectar documentos que prometen comportamientos que ya no existen.
5. Verificar que cada decisión esté documentada en el archivo correcto.
6. Eliminar duplicación innecesaria.
7. Mantener cada documento dentro de su responsabilidad.
```

Documentos que especialmente necesitan revisión por las decisiones
recién cerradas:

``` text
domain.md
api.md
order-flow.md
use-cases.md
security-authorization.md
decisions.md
product-catalog.md
synchronization.md
event-consumption.md
README.md
roadmap.md
future.md
```

El objetivo de esta revisión es llegar a:

``` text
12 documentos coherentes
        ↓
DISEÑO CERRADO
        ↓
IMPLEMENTACIÓN
```

------------------------------------------------------------------------

# 24. Importante para el nuevo chat

Al iniciar el nuevo chat, la instrucción debe ser:

> Continuemos el diseño de Order Service desde este contexto maestro.
> Los puntos 1--10 ya están cerrados. No los vuelvas a discutir ni
> propongas alternativas salvo que encontremos una contradicción real en
> los documentos. El siguiente paso es revisar cruzadamente los 12
> documentos actuales de `order-service`, uno por uno, contra las
> decisiones de este contexto. Primero detectar contradicciones y
> faltantes; después actualizar la documentación. No implementar
> todavía.

Si se detecta una contradicción real, seguir esta regla:

``` text
Documento actual
      ↓
¿Contradice una decisión cerrada?
      ↓
Sí
      ↓
Identificar contradicción
      ↓
Proponer corrección mínima
      ↓
Actualizar únicamente el documento responsable
```

No reabrir el diseño completo por una contradicción documental.

------------------------------------------------------------------------

# 25. Principio de trabajo

``` text
¿Qué problema resolvemos?
        ↓
¿Qué decisión ya está tomada?
        ↓
¿Dónde pertenece esa responsabilidad?
        ↓
¿El documento la refleja?
        ↓
Corregir documentación
        ↓
Continuar
```

No agregar complejidad solamente porque podría ser necesaria en el
futuro.

Diseñar para evolucionar, pero implementar solamente lo necesario.
