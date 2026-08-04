# Order Service

## Descripción

Order Service es responsable de gestionar el ciclo de vida de las órdenes dentro del ecosistema **Ecommerce Microservices Demo**.

Este servicio implementa el dominio de las órdenes de compra y mantiene una copia local del catálogo de productos sincronizada mediante eventos publicados por **Product Service**.

El objetivo es desacoplar el proceso de compra del catálogo, permitiendo validar productos de forma local sin depender de llamadas síncronas entre microservicios.

---

# Responsabilidades

Actualmente el servicio es responsable de:

- Gestionar el ciclo de vida de las órdenes.
- Mantener una proyección local del catálogo de productos (`ProductCatalog`).
- Consumir eventos publicados por Product Service.
- Validar productos antes de crear una orden.
- Persistir órdenes y sus ítems.
- Publicar eventos relacionados con las órdenes (próximamente).

---

# Arquitectura

Actualmente Order Service implementa una arquitectura basada en eventos.

```text
                 RabbitMQ

                      ▲

                      │

            Product Service

          (Product Events)

                      │

                      ▼

               Order Service

                      │

          Product Catalog Projection

                      │

               Create Order
```

El catálogo local (`ProductCatalog`) se mantiene sincronizado mediante eventos provenientes de Product Service.

Esto evita consultas REST durante la creación de una orden y reduce el acoplamiento entre servicios.

---

# Principios de Diseño

Este servicio fue diseñado siguiendo los siguientes principios:

- Domain-Driven Design (DDD)
- Event-Driven Architecture (EDA)
- Local Projection Pattern
- Aggregate Root
- Snapshot histórico de las órdenes
- Bajo acoplamiento entre microservicios

---

# Funcionalidades

## Implementadas

- Consumo de eventos RabbitMQ.
- Sincronización del catálogo local.
- Persistencia de ProductCatalog.
- Logging estructurado.
- Propagación de TraceId.

## En desarrollo

- Creación de órdenes.
- Modelo de dominio Order.
- Persistencia de OrderItem.

## Futuro

- Publicación de eventos de órdenes.
- Integración con Payment Service.
- Reserva de inventario.
- Retry.
- Dead Letter Queue.
- Outbox Pattern.
- Saga Pattern.

---

# Documentación

La documentación del servicio se encuentra en:

```text
docs/services/order-service/
```

Documentos principales:

- overview.md
- domain.md
- create-order.md
- rabbitmq.md
- roadmap.md

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

# Estado

🚧 En desarrollo.

Actualmente el servicio mantiene sincronizada una proyección local del catálogo de productos y se encuentra evolucionando hacia la implementación completa del dominio de órdenes.