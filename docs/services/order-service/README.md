# Order Service

## Descripción

Order Service gestiona el ciclo de vida de las órdenes dentro del ecosistema **Ecommerce Microservices Demo**.

El servicio representa el dominio de las órdenes de compra y mantiene una proyección local del catálogo de productos (`ProductCatalog`), sincronizada mediante eventos publicados por Product Service.

El objetivo arquitectónico es desacoplar el proceso de compra del catálogo, permitiendo que Order Service trabaje con información local sin realizar llamadas síncronas a Product Service durante la creación de una Order.

---

# Responsabilidades

Order Service es responsable de:

- Gestionar el ciclo de vida de las Orders.
- Mantener la proyección local `ProductCatalog`.
- Consumir eventos publicados por Product Service.
- Utilizar `ProductCatalog` para validar productos durante los casos de uso.
- Construir y persistir Orders y sus `OrderItem`.
- Aplicar las reglas del dominio de Order.
- Aplicar las reglas de autorización propias de sus casos de uso.
- Garantizar la idempotencia de `POST /orders` mediante `Idempotency-Key`.
- Publicar eventos propios de Order como evolución futura.

Order Service no es responsable de:

- ser la fuente de verdad del catálogo;
- gestionar directamente productos;
- gestionar pagos;
- reservar inventario;
- gestionar pricing/offer como capability independiente;
- contener detalles de Spring Security dentro del dominio.

---

# Arquitectura

El modelo actual combina un dominio transaccional con una proyección local sincronizada mediante eventos.

```text
                         Product Service
                               │
                               │ Product Events
                               ▼
                            RabbitMQ
                               │
                               ▼
                    ProductCatalogConsumer
                               │
                               ▼
                     ProductCatalogService
                               │
                               ▼
                       ProductCatalog
                               │
                               │ lectura
                               ▼
Client ──► Order Application ──► Order
                    │              │
                    │              └── OrderItem
                    │
                    └── Authorization
```

Product Service continúa siendo el **Source of Truth** del catálogo.

`ProductCatalog` es una proyección local utilizada por Order Service.

La sincronización y el consumo de eventos se documentan en:

- `product-catalog.md`
- `synchronization.md`
- `event-consumption.md`

---

# Modelo de Dominio

`Order` es el **Aggregate Root** del dominio.

```text
Order
│
├── id
├── customerId
│
├── OrderItem
│    ├── productId
│    ├── productName
│    ├── unitPrice
│    ├── quantity
│    └── subtotal
│
├── total
├── status
├── createdAt
└── updatedAt
```

Cada `OrderItem` conserva un snapshot de la información comercial utilizada para la compra.

El `total` es calculado por `Order` y persistido como parte de su estado.

El `id` de `Order` es incremental y gestionado por la base de datos.

La Order no depende posteriormente del catálogo para reconstruir su historial.

Los detalles del modelo, invariantes, estados y comportamiento se encuentran en:

```text
domain.md
```

---

# Casos de Uso

Los casos de uso principales definidos para Order Service son:

```text
Create Order
Get Order
List Orders
Cancel Order
```

El caso de uso coordina:

- actor autenticado;
- autorización;
- ownership;
- `Idempotency-Key`;
- consolidación de productos duplicados;
- `ProductCatalog`;
- Aggregate;
- persistencia.

Las reglas internas del Aggregate permanecen dentro del dominio.

Los casos de uso se documentan en:

```text
use-cases.md
```

---

# Seguridad y Autorización

El diseño separa:

```text
Authentication
      ↓
Authorization
      ↓
Ownership
      ↓
Domain Rules
```

El actor autenticado no se introduce directamente en el dominio como una dependencia de Spring Security o JWT.

Además:

```text
Actor ≠ Customer
```

Un `USER` opera sobre sus propias Orders, mientras que un `ADMIN` puede disponer de capacidades administrativas adicionales.

Los detalles se documentan en:

```text
security-authorization.md
```

---

# API

La API expone los contratos HTTP de Order Service.

Operaciones principales:

```text
POST /orders
GET  /orders/{id}
GET  /orders
PATCH /orders/{id}/cancel
```

Las operaciones y sus DTOs se encuentran documentados en:

```text
api.md
```

---

# Flujo Principal

El flujo conceptual de creación es:

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
Authorization / Ownership
  │
  ▼
Consolidate duplicate products
  │
  ▼
ProductCatalog
  │
  ▼
Validate products and known stock
  │
  ▼
Build OrderItems
  │
  ▼
Build Order
  │
  ▼
Calculate subtotals + total
  │
  ▼
Persist Order + Items + IdempotencyRecord
  │
  ▼
COMMIT
  │
  ▼
PENDING_PAYMENT
```

La creación de la Order no consulta Product Service mediante REST.

La validación de stock utiliza el stock conocido por `ProductCatalog`; no representa una reserva de inventario.

Los productos duplicados se consolidan antes de construir los `OrderItem`.

El flujo completo se documenta en:

```text
order-flow.md
```

---

# Integración con Product Service

Product Service es la fuente de verdad del catálogo.

Order Service mantiene únicamente la información necesaria en `ProductCatalog`.

```text
Product Service
      │
      │ events
      ▼
ProductCatalog
      │
      ▼
Order Service
```

Esta comunicación utiliza consistencia eventual.

La proyección puede representar temporalmente un estado anterior mientras un evento está pendiente de procesamiento.

---

# Estado del Proyecto

## Implementado

- Consumo de eventos RabbitMQ.
- Sincronización del catálogo local.
- Persistencia de `ProductCatalog`.
- Logging estructurado.
- Propagación de TraceId.

## Diseño definido

- Aggregate `Order`.
- `OrderItem` como snapshot histórico.
- `OrderStatus` y State Machine.
- Invariantes del dominio.
- `Order.id` incremental gestionado por la base de datos.
- Cálculo y persistencia de `Order.total`.
- Consolidación de productos duplicados.
- Validación de existencia y stock conocido sin reserva.
- Precio como snapshot y aceptación de consistencia eventual.
- `customerId` como identificador de ownership.
- Casos de uso.
- Authorization y ownership.
- API y DTOs definitivos.
- `Idempotency-Key` obligatorio para `POST /orders`.
- Persistencia de idempotencia.
- Flujo de creación de Order.
- Evolución de Order Events.

## Próxima implementación

- Crear `Order` y `OrderItem`.
- Implementar Create Order Use Case.
- Implementar consolidación de productos duplicados.
- Implementar persistencia de Order + OrderItems + IdempotencyRecord.
- Implementar consultas y `OrderSummaryResponse`.
- Implementar lifecycle de Order.
- Implementar `PATCH /orders/{id}/cancel`.
- Implementar pruebas del dominio y casos de uso.
- Implementar pruebas de idempotencia.

El estado detallado y el orden de implementación se encuentran en:

```text
roadmap.md
```

---

# Evolución Futura

La arquitectura está preparada para evolucionar hacia:

```text
Order Events
      │
      ├──► Payment Service
      ├──► Inventory Service
      ├──► Notification Service
      └──► Analytics
```

También se contemplan, cuando exista una necesidad concreta:

- Inventory / Reservation;
- Payment;
- Pricing / Offer;
- Order Events;
- Retry;
- Dead Letter Queue;
- Publisher Confirms;
- Consumer Idempotency;
- Idempotencia end-to-end;
- Outbox Pattern;
- Saga Pattern;
- Customer Projection;
- observabilidad distribuida.

La idempotencia HTTP mediante `Idempotency-Key` para `POST /orders` ya forma parte del MVP y no se considera una capacidad futura.

La visión arquitectónica futura se encuentra en:

```text
future.md
```

---

# Documentación

La documentación de Order Service está separada por responsabilidad:

```text
order-service/
│
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

## Responsabilidad de cada documento

| Documento | Responsabilidad |
|---|---|
| `README.md` | Visión general del servicio |
| `domain.md` | Dominio, Aggregate, invariantes y comportamiento |
| `use-case.md` | Casos de uso |
| `security-authorization.md` | Actor, autorización y ownership |
| `api.md` | Contratos HTTP y DTOs |
| `order-flow.md` | Flujos principales |
| `product-catalog.md` | Modelo y límites de ProductCatalog |
| `synchronization.md` | Estrategia de sincronización |
| `event-consumption.md` | Consumo de eventos |
| `decisions.md` | Decisiones y razones |
| `roadmap.md` | Estado y orden de evolución |
| `future.md` | Arquitectura futura |

La intención es evitar duplicar la documentación entre archivos.

Cada documento responde una pregunta diferente sobre Order Service.

---

# Tecnologías

- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Spring Security
- JWT
- Log4j2
- Maven

---

# Principios de Diseño

Order Service se desarrolla siguiendo:

- Domain-Driven Design (DDD);
- Event-Driven Architecture (EDA);
- Local Projection Pattern;
- Aggregate Root;
- Snapshot histórico;
- bajo acoplamiento;
- separación de responsabilidades;
- evolución incremental;
- consistencia eventual donde el desacoplamiento lo requiera;
- separación entre validación de stock y reserva de inventario;
- idempotencia en operaciones con efectos persistentes.

La regla general del proyecto es:

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

> Diseñar para evolucionar, pero implementar solamente lo que necesita el negocio actual.

---

# Estado

🚧 **En desarrollo**

Actualmente Order Service mantiene una proyección local del catálogo de productos y se encuentra en proceso de implementación del dominio completo de Orders.
