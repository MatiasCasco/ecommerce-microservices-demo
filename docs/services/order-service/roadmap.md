# Order Service Roadmap

## Objetivo

Este documento describe la evolución planificada de Order Service.

El roadmap separa:

- lo que ya existe en infraestructura;
- lo que estamos definiendo en el diseño;
- lo que será implementado posteriormente;
- las evoluciones futuras.

Cada fase debe construirse sobre decisiones previamente documentadas.

La prioridad es mantener una arquitectura simple, consistente y evolutiva, evitando implementar funcionalidades antes de cerrar su diseño.

---

# Estado Actual

## Infraestructura y sincronización existentes

- [x] ProductCatalog Projection
- [x] RabbitMQ Consumer
- [x] ProductCatalogService
- [x] Sincronización mediante eventos
- [x] Base espejo de productos
- [x] Logging estructurado
- [x] TraceId

Estas capacidades constituyen la infraestructura necesaria para que Order Service pueda trabajar con una proyección local del catálogo.

---

# Fase 1 — Diseño del Dominio

## Objetivo

Definir completamente el modelo de dominio antes de comenzar la implementación.

### Estado

- [x] Definir Order como Aggregate Root
- [x] Definir OrderItem
- [x] Definir OrderItem como snapshot histórico
- [x] Definir OrderStatus
- [x] Definir invariantes
- [x] Definir inmutabilidad del contenido comercial
- [x] Definir transiciones de estado
- [x] Definir estado inicial `PENDING_PAYMENT`
- [x] Definir `Order.id` generado incrementalmente por la base de datos
- [x] Definir cálculo y persistencia del `Order.total`
- [x] Definir consolidación de productos duplicados
- [x] Definir validación de stock sin reserva
- [x] Definir precio como snapshot y aceptar consistencia eventual
- [x] Definir `customerId` como ownership
- [x] Definir DTOs de Create/Response/Summary
- [x] Definir errores de negocio
- [x] Definir idempotencia de `POST /orders`
- [x] Definir State Machine completa

Documentación principal:

- `domain.md`
- `decisions.md`

---

# Fase 2 — Diseño de Application y Seguridad

## Objetivo

Definir cómo se ejecutan los casos de uso sin contaminar el dominio con detalles de seguridad o infraestructura.

### Estado

- [x] Separar Actor de Customer
- [x] Definir ownership
- [x] Definir diferencia entre USER y ADMIN
- [x] Definir separación Authentication / Authorization / Business Rules
- [x] Definir responsabilidad de Application
- [x] Diseñar Create Order Use Case

Documentación principal:

- `use-cases.md`
- `security-authorization.md`

---

# Fase 3 — Diseño de API

## Objetivo

Definir el contrato externo de Order Service una vez establecido el modelo de dominio y el caso de uso.

### Estado

- [x] Definir `POST /orders`
- [x] Definir `GET /orders/{id}`
- [x] Definir `GET /orders`
- [x] Definir `PATCH /orders/{id}/cancel`
- [x] Definir reglas generales del request
- [x] Definir responsabilidades de DTOs
- [x] Definir `Idempotency-Key` obligatorio para `POST /orders`
- [x] Definir `OrderResponse`
- [x] Definir `OrderSummaryResponse`
- [x] Definir consolidación de productos duplicados en Create Order
- [ ] Completar contrato técnico OpenAPI/Swagger

Documentación principal:

- `api.md`

---

# Fase 4 — Implementación del Create Order

## Objetivo

Implementar el caso de uso principal utilizando las decisiones cerradas en las fases anteriores.

### Pendiente

- [ ] Implementar `Order`
- [ ] Implementar `OrderItem`
- [ ] Implementar `OrderStatus`
- [ ] Implementar invariantes
- [ ] Implementar comportamiento del Aggregate
- [ ] Implementar Create Order Use Case
- [ ] Integrar `ProductCatalog`
- [ ] Implementar persistencia de Order + OrderItems
- [ ] Implementar persistencia de `IdempotencyRecord`
- [ ] Implementar transacción de Order + OrderItems + IdempotencyRecord
- [ ] Implementar consolidación de productos duplicados
- [ ] Implementar DTOs
- [ ] Implementar Controller
- [ ] Implementar manejo de errores
- [ ] Implementar pruebas unitarias del dominio
- [ ] Implementar pruebas del caso de uso
- [ ] Implementar pruebas de integración
- [ ] Probar retry con la misma `Idempotency-Key`
- [ ] Probar rechazo de misma key con request diferente

---

# Fase 5 — Consultas de Orders

## Objetivo

Implementar las operaciones de lectura de Orders.

### Pendiente

- [ ] Obtener Order por ID
- [ ] Listar Orders
- [ ] Paginación
- [ ] Ordenamiento
- [ ] Definir filtros cuando exista una necesidad concreta
- [ ] Validar ownership para consultas de USER
- [ ] Definir permisos administrativos

---

# Fase 6 — Lifecycle de Order

## Objetivo

Implementar las transiciones de estado definidas por el dominio.

### Pendiente

- [ ] Implementar `PENDING_PAYMENT`
- [ ] Implementar transición a `CANCELLED`
- [ ] Implementar transición a `PAID` cuando exista el flujo de Payment
- [ ] Impedir transiciones inválidas
- [ ] Implementar `PATCH /orders/{id}/cancel`
- [ ] Agregar pruebas de la State Machine

---

# Fase 7 — Order Events

## Objetivo

Introducir la publicación de eventos propios de Order después de estabilizar el caso de uso principal.

### Pendiente

- [ ] Definir `OrderCreatedEvent`
- [ ] Definir contrato de eventos
- [ ] Definir exchange
- [ ] Definir routing keys
- [ ] Implementar publisher
- [ ] Implementar publicación
- [ ] Definir estrategia de errores
- [ ] Definir observabilidad del publisher

Evento inicial previsto:

```text
ORDER_CREATED
```

La implementación debe mantener desacoplados los consumidores futuros del Aggregate.

---

# Fase 8 — Notification

## Objetivo

Consumir eventos de Order para desacoplar las notificaciones del proceso de creación.

### Futuro

- [ ] Notification Service
- [ ] Consumo de `ORDER_CREATED`
- [ ] Envío de notificaciones
- [ ] Manejo de errores
- [ ] Retry
- [ ] DLQ

---

# Fase 9 — Payment

## Objetivo

Introducir el proceso de pago como una responsabilidad independiente.

### Futuro

- [ ] Payment Service
- [ ] Flujo de pago
- [ ] Confirmación de pago
- [ ] Integración mediante eventos
- [ ] Transición `PENDING_PAYMENT → PAID`
- [ ] Manejo de errores de pago
- [ ] Idempotencia del procesamiento

La Order no debe asumir que fue pagada simplemente porque fue creada.

---

# Fase 10 — Inventory / Reservation

## Objetivo

Resolver la reserva de inventario y el problema de concurrencia sobre stock.

### Futuro

- [ ] Inventory Service
- [ ] Reserva de stock
- [ ] Liberación de reserva
- [ ] Confirmación de reserva
- [ ] Manejo de concurrencia
- [ ] Resolver overselling
- [ ] Integración con el lifecycle de Order

Principio:

```text
Order CREATED
      ≠
Stock RESERVED
```

La reserva no debe implementarse como una responsabilidad accidental dentro de Order Service.

---

# Fase 11 — Resiliencia y Mensajería

## Objetivo

Fortalecer la comunicación mediante eventos.

### Futuro

- [ ] Retry
- [ ] Consumer Retry
- [ ] Dead Letter Queue
- [ ] Publisher Confirms
- [ ] Idempotencia de consumidores
- [ ] Manejo de mensajes duplicados
- [ ] Observabilidad de procesamiento

### Aclaración sobre idempotencia

La idempotencia de creación de Order mediante:

```text
Idempotency-Key
```

forma parte del MVP y se implementa durante la Fase 4.

Esta fase se refiere a la idempotencia de consumidores y mensajes, que resuelve problemas diferentes:

```text
HTTP Idempotency
POST /orders
        ↓
evitar Orders duplicadas por retry del cliente

Consumer Idempotency
evento / mensaje
        ↓
evitar efectos duplicados por redelivery
```

---

# Fase 12 — Consistencia y Arquitectura Avanzada

## Objetivo

Resolver problemas de consistencia entre persistencia y publicación de eventos cuando la complejidad del sistema lo justifique.

### Futuro

- [ ] Outbox Pattern
- [ ] Saga Pattern
- [ ] Reconciliación
- [ ] Idempotencia end-to-end
- [ ] Observabilidad distribuida
- [ ] Customer Projection si aparece una necesidad concreta
- [ ] Pricing / Offer si aparecen necesidades de pricing más sofisticadas

Estas capacidades no deben implementarse anticipadamente sin un problema real que las justifique.

---

# Orden de Implementación

El camino principal queda:

```text
Diseño del dominio
        ↓
Application / Security
        ↓
API
        ↓
Create Order
        │
        ├── Idempotency-Key
        ├── consolidación de items
        ├── ProductCatalog
        └── Order + OrderItems + IdempotencyRecord
        ↓
Consultas
        ↓
Lifecycle
        ↓
Order Events
        ↓
Notification
        ↓
Payment
        ↓
Inventory / Reservation
        ↓
Resiliencia de mensajería
        ↓
Outbox / Saga / consistencia avanzada
```

La idempotencia HTTP de `POST /orders` no queda como una etapa futura: forma parte de la implementación de Create Order.

La idempotencia de consumidores y la idempotencia end-to-end pertenecen a las fases posteriores de mensajería y consistencia.

---

# Principio del Roadmap

El roadmap no representa solamente una lista de funcionalidades.

Cada etapa debe responder a una necesidad concreta y construirse sobre las decisiones anteriores.

```text
Problema
   ↓
Análisis
   ↓
Decisión
   ↓
Documentación
   ↓
Implementación
   ↓
Pruebas
   ↓
Evolución
```

La regla general es:

> Diseñar para evolucionar, pero implementar solamente lo que necesita el negocio actual.
