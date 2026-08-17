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
- Publicar eventos propios de Order como evolución futura.

Order Service no es responsable de:

- ser la fuente de verdad del catálogo;
- gestionar directamente productos;
- gestionar pagos;
- reservar inventario;
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
│
└── status
```

Cada `OrderItem` conserva un snapshot de la información comercial utilizada para la compra.

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
Create Order
  │
  ▼
Authorization
  │
  ▼
ProductCatalog
  │
  ▼
Validate Products
  │
  ▼
Build OrderItems
  │
  ▼
Build Order
  │
  ▼
Calculate totals
  │
  ▼
Persist Aggregate
  │
  ▼
PENDING_PAYMENT
```

La creación de la Order no consulta Product Service mediante REST.

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
- `OrderItem`.
- `OrderStatus`.
- Invariantes del dominio.
- Casos de uso.
- Authorization y ownership.
- API.
- Flujo de creación de Order.
- Evolución de Order Events.

## Próxima implementación

- Crear `Order` y `OrderItem`.
- Implementar Create Order Use Case.
- Implementar persistencia de Order.
- Implementar consultas.
- Implementar lifecycle de Order.
- Implementar pruebas del dominio y casos de uso.

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

- Inventory Reservation;
- Payment;
- Retry;
- Dead Letter Queue;
- Publisher Confirms;
- Idempotencia;
- Outbox Pattern;
- Saga Pattern;
- Customer Projection;
- observabilidad distribuida.

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
| `use-cases.md` | Casos de uso |
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
- evolución incremental.

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
