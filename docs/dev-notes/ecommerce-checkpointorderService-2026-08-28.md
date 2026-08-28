# CHECKPOINT — ECOMMERCE MICROSERVICES
## Contexto de continuidad — 28/08/2026

## 1. Objetivo
Proyecto ecommerce con Java/Spring Boot, PostgreSQL, RabbitMQ, `common-lib`, logging estructurado (`CommerceLog`) y MDC/`traceId`.

Servicios principales:
- `product-service`
- `user-service`
- `order-service`

Decisión: `order-service` será el módulo donde experimentaremos Hexagonal Architecture manteniendo el comportamiento funcional existente.

Principio:
> Implementar lo necesario cuando tenga peso real.

---

## 2. Proyección local ProductCatalog

`Product Service` es el Source of Truth. `Order Service` mantiene una proyección local:

```text
Product Service -> RabbitMQ -> Order Service -> ProductCatalog -> PostgreSQL
```

La consistencia es eventualmente consistente.

Eventos actuales:
- `PRODUCT_CREATED`
- `PRODUCT_UPDATED`
- `PRODUCT_ACTIVATED`
- `PRODUCT_DEACTIVATED`
- `PRODUCT_STOCK_UPDATED`

Colas actuales:
- `product.created.queue`
- `product.updated.queue`
- `product.activated.queue`
- `product.deactivated.queue`
- `product.stock.updated.queue`

No modificar las cinco colas durante esta migración.

---

## 3. Migración a Hexagonal

Estructura acordada:

```text
order-service
|
├── domain
│   ├── model
│   │   └── ProductCatalog.java
│   └── enums
│       └── ProductStatus.java
|
├── application
│   ├── port
│   │   ├── in
│   │   │   └── ProductCatalogSynchronizationUseCase.java
│   │   └── out
│   │       └── ProductCatalogRepository.java
│   └── service
│       └── ProductCatalogSynchronizationService.java
|
├── adapter
│   ├── in
│   │   └── consumer
│   │       └── ProductCatalogConsumer.java
│   └── out
│       └── persistence
│           ├── entity
│           │   └── ProductCatalogEntity.java
│           ├── ProductCatalogJpaRepository.java
│           └── ProductCatalogPersistenceAdapter.java
|
└── config
    └── RabbitMQConfig.java
```

Responsabilidades:

- `ProductCatalog` es modelo de dominio y no conoce JPA.
- `ProductCatalogEntity` es entidad de persistencia y sí conoce JPA.
- `ProductCatalogRepository` es Output Port y NO extiende `JpaRepository`.
- `ProductCatalogJpaRepository` es infraestructura Spring Data.
- `ProductCatalogPersistenceAdapter` implementa el Output Port y mapea `ProductCatalog <-> ProductCatalogEntity`.
- `ProductCatalogSynchronizationService` reemplaza conceptualmente a `ProductCatalogServiceImpl` y coordina la sincronización.
- `ProductCatalogSynchronizationUseCase` es Input Port.
- `ProductCatalogConsumer` es Input Adapter de RabbitMQ.

Flujo:

```text
RabbitMQ
  -> ProductCatalogConsumer
  -> ProductCatalogSynchronizationUseCase
  -> ProductCatalogSynchronizationService
  -> ProductCatalogRepository
  -> ProductCatalogPersistenceAdapter
  -> ProductCatalogJpaRepository
  -> ProductCatalogEntity
  -> PostgreSQL
```

No crear abstracciones innecesarias.

---

## 4. Pruebas funcionales realizadas

Se probó el ecosistema completo.

### Producto 18
`PRODUCT_CREATED` -> proyección correcta:
- id 18
- stock 20
- status ACTIVE

### Producto 19
Secuencia:
`PRODUCT_CREATED -> PRODUCT_UPDATED -> PRODUCT_UPDATED`

Estado final correcto:
- id 19
- price 125000
- stock 20
- status ACTIVE
- name `Cable multifilar de 2 mm Impaco`

### Producto 20
Secuencia:
`PRODUCT_CREATED -> PRODUCT_STOCK_UPDATED -> PRODUCT_STOCK_UPDATED -> PRODUCT_STOCK_UPDATED -> PRODUCT_DEACTIVATED -> PRODUCT_ACTIVATED`

Estado final correcto:
- id 20
- stock 25
- status ACTIVE

Conclusión funcional:
- PRODUCT_CREATED OK
- PRODUCT_UPDATED OK
- PRODUCT_ACTIVATED OK
- PRODUCT_DEACTIVATED OK
- PRODUCT_STOCK_UPDATED OK

Productos históricos 10–12 no presentes en la proyección NO son bug: fueron creados antes de comenzar la proyección local.

---

## 5. Observabilidad

### Punto 1 — MDC / traceId

Problema detectado:

```text
Product Service
[traceId=ABC]
      |
      v
RabbitMQ
      |
      v
Order Service
[traceId=]
```

El evento sí contiene `event.traceId`.

Solución implementada en `ProductCatalogConsumer`:

```java
private void executeWithTraceId(String traceId, Runnable action) {
    try {
        MDC.put(TraceConstants.TRACE_ID, traceId);
        action.run();
    } finally {
        MDC.remove(TraceConstants.TRACE_ID);
    }
}
```

Los cinco listeners utilizan este método.

No repetir `MDC.put/remove` cinco veces.
No usar AOP para este caso.

El `finally` es importante porque los threads de RabbitMQ se reutilizan.

### Punto 2 — log de procesamiento exitoso

Actualmente existe:

```text
PRODUCT_EVENT_RECEIVED
```

Pendiente agregar/verificar después de que el UseCase termine correctamente:

```text
PRODUCT_CATALOG_SYNCHRONIZED
```

Mismo patrón para los cinco eventos.

Debe incluir para correlación:
- `eventType`
- `eventId`
- `aggregateId`
- `traceId`

La duplicación del bloque de `CommerceLog` no es una preocupación actualmente.

---

## 6. Common-lib y excepciones

Existe una jerarquía compartida:

```text
BaseException
    |
    v
BusinessException
    |
    v
GlobalExceptionHandler
```

Product Service utiliza `BusinessException` con su `ProductErrorCode`.

`GlobalExceptionHandler` transforma excepciones del flujo HTTP en `ErrorResponse`.

Importante:
- `GlobalExceptionHandler` es para HTTP.
- No equivale al manejo de errores de RabbitMQ.

No crear ahora:
- `OrderErrorCode`
- `ProductCatalogNotFoundException`
- nueva jerarquía de excepciones
- RabbitMQ Error Handler

---

## 7. EntityNotFoundException pendiente

Actualmente existe un punto a limpiar:

```java
private ProductCatalog findProduct(Long productId) {
    return productCatalogRepository.findById(productId)
            .orElseThrow(() ->
                    new EntityNotFoundException(
                            PRODUCT_NOT_FOUND + productId
                    ));
}
```

Problema:

```text
Application -> jakarta.persistence.EntityNotFoundException
```

Eso rompe el desacoplamiento Hexagonal.

Todavía NO se decidió reemplazarlo por una nueva excepción.

La decisión debe hacerse cuando se diseñe correctamente el manejo de errores del consumidor RabbitMQ, manteniendo coherencia con `common-lib`.

No crear una excepción solo por anticipación.

---

## 8. Temas futuros

### Idempotencia
Actualmente no implementada.

Caso a resolver en el futuro:
```text
same eventId
    -> message received twice
    -> ¿procesamos dos veces?
```

### Ordering
Se identificó como punto arquitectónicamente importante.

Actualmente no existe estrategia explícita para eventos fuera de orden.

Ejemplo:
```text
CREATED -> UPDATED -> STOCK_UPDATED
```
podría llegar:
```text
UPDATED -> STOCK_UPDATED -> CREATED
```

No cambiar las cinco colas ahora.

### Retry / DLQ
No implementado.

Futuro:
```text
Consumer -> failure -> retry -> failure -> DLQ
```

### Outbox / transacciones distribuidas
No implementar todavía.

Create Order tendrá una transacción local prevista:

```text
BEGIN
  Order
  OrderItems
  IdempotencyRecord
COMMIT
```

Saga/Outbox se evaluarán cuando exista peso real.

---

## 9. Decisiones cerradas

- Order Service experimentará Hexagonal Architecture.
- ProductCatalog se separa de JPA.
- ProductCatalogEntity pertenece a persistence adapter.
- ProductCatalogRepository es Output Port.
- ProductCatalogJpaRepository es infraestructura.
- ProductCatalogPersistenceAdapter implementa Output Port.
- ProductCatalogConsumer es Input Adapter.
- ProductCatalogSynchronizationService es Application Service.
- Se mantienen las cinco colas.
- Se mantienen los cinco eventos.
- No introducir todavía retry/DLQ/idempotencia/ordering.
- No crear OrderErrorCode todavía.
- No crear nueva jerarquía de excepciones todavía.
- No usar AOP solamente para MDC.
- MDC se establece mediante `executeWithTraceId()`.
- La duplicación del bloque `CommerceLog` se acepta.

---

## 10. Estado actual

```text
HEXAGONAL PROJECTION
Domain separado de JPA       OK
Input Port                   OK
Output Port                  OK
RabbitMQ Adapter             OK
Persistence Adapter          OK
JPA Entity separada          OK
Funcionalidad                OK
Pruebas reales               OK

OBSERVABILIDAD
traceId -> MDC               SOLUCIONADO
PROCESS SUCCESS LOG          PENDIENTE

RESILIENCIA
Idempotencia                 FUTURO
Ordering                     FUTURO
Retry                        FUTURO
DLQ                          FUTURO
Outbox                       FUTURO
Saga                         FUTURO

EXCEPCIONES
BusinessException            EXISTENTE EN common-lib
GlobalExceptionHandler       EXISTENTE
OrderErrorCode               NO CREAR AHORA
RabbitMQ error handling      FUTURO
```

---

## 11. Próximo paso

1. Terminar/verificar `PRODUCT_CATALOG_SYNCHRONIZED` para los cinco eventos.
2. Ejecutar nuevamente:
   - PRODUCT_CREATED
   - PRODUCT_UPDATED
   - PRODUCT_STOCK_UPDATED
   - PRODUCT_DEACTIVATED
   - PRODUCT_ACTIVATED
3. Verificar logs:
```text
[traceId=...]
PRODUCT_EVENT_RECEIVED
...
PRODUCT_CATALOG_SYNCHRONIZED
```
4. Confirmar `catalog_products`.
5. Revisar que Application no tenga dependencias accidentales de JPA, entities de persistencia o JpaRepository.
6. Dar por cerrada la migración de la proyección.
7. Continuar con `Order`:
```text
Domain
  -> Order
  -> OrderItem
  -> OrderStatus
  -> CreateOrderUseCase
```

---

## 12. Contexto para un nuevo chat

Al iniciar un nuevo chat, asumir:

> Estamos trabajando sobre `ecommerce-microservices-demo`. El diseño general ya fue realizado. No debemos rediseñar desde cero.

> `order-service` está siendo migrado progresivamente a Hexagonal Architecture como experimento interno, manteniendo el comportamiento existente.

> La primera parte migrada es la sincronización de la proyección local `ProductCatalog`.

> La migración funcional está correcta y fue validada con los cinco eventos.

> El `traceId` llegaba dentro del evento pero no estaba en MDC. Se solucionó mediante `executeWithTraceId()` sin repetir `MDC.put/remove` cinco veces.

> El siguiente punto de observabilidad es `PRODUCT_CATALOG_SYNCHRONIZED` después de que el UseCase termine exitosamente, para los cinco eventos.

> No crear todavía `OrderErrorCode`, nuevas jerarquías de excepciones, retry, DLQ, idempotencia, ordering, Outbox o Saga.

> Mantener coherencia con `common-lib`, `CommerceLog`, `BusinessException`, `BaseException`, `GlobalExceptionHandler` y `TraceConstants`.

> Una vez cerrada la proyección, continuar con la implementación de `Order` siguiendo el diseño Hexagonal ya definido.

---

## 13. Regla de trabajo

```text
Código actual
     |
     v
Identificar comportamiento
     |
     v
Migrar una frontera
     |
     v
Compilar
     |
     v
Probar
     |
     v
Confirmar
     |
     v
Continuar
```

No hacer reescrituras masivas.
No introducir abstracciones sin necesidad.
No modificar decisiones cerradas sin una contradicción real.

Objetivo:

> Poder explicar técnicamente por qué una parte necesita Hexagonal y qué problema concreto resolvió.
