# 📐 Design Patterns
| Pattern    | Implementado en                      | Propósito                               |
| ---------- | ------------------------------------ | --------------------------------------- |
| DTO        | APIs                                 | Desacoplar entidades del contrato REST  |
| Repository | Todos los servicios                  | Acceso a datos mediante Spring Data JPA |
| Factory    | Product Service                      | Construir eventos de dominio            |
| Publisher  | Product Service                      | Publicar eventos en RabbitMQ            |
| Consumer   | Order Service / Notification Service | Procesar eventos recibidos              |
| Retry      | Notification Service                 | Reintentar operaciones fallidas         |
| Scheduler  | Notification Service                 | Reprocesar eventos pendientes           |


---
## 🚧 Patrones planificados

Los siguientes patrones forman parte de la evolución de la arquitectura y aún no han sido implementados.

| Pattern | Objetivo |
|----------|----------|
| Outbox Pattern | Garantizar la publicación de eventos |
| Dead Letter Queue | Manejar eventos que no pudieron procesarse |
| Publisher Confirms | Confirmar la entrega de mensajes a RabbitMQ |
| Idempotency | Evitar el procesamiento duplicado de eventos |
| Circuit Breaker | Proteger llamadas REST entre servicios |
---
## 🧠 Explicación
## Publisher Pattern

Permite publicar eventos de dominio sin acoplar la lógica de negocio al mecanismo de mensajería.

En este proyecto:

- Product Service publica eventos hacia RabbitMQ.
- Los productores no conocen qué servicios consumirán esos eventos.

Eventos publicados:

- ProductCreatedEvent
- ProductUpdatedEvent
- ProductActivatedEvent
- ProductDeactivatedEvent
- ProductStockUpdatedEvent

## Consumer Pattern

Permite reaccionar a eventos publicados por otros servicios de forma asíncrona.

En este proyecto:

- Order Service sincroniza ProductCatalog.
- Notification Service genera notificaciones.

Los consumidores permanecen desacoplados del productor y procesan únicamente los eventos que les corresponden.

## DTO Pattern

Se utiliza para desacoplar el modelo interno del contrato expuesto por las APIs REST.

Evita exponer directamente las entidades JPA y facilita la evolución independiente de la capa de persistencia y la capa de presentación.

## Repository Pattern

Abstrae el acceso a la base de datos mediante Spring Data JPA.

Permite que la lógica de negocio permanezca independiente de la tecnología de persistencia utilizada.

## Retry Pattern

Permite reintentar operaciones que fallan por causas temporales.

En Notification Service se utiliza para reprocesar operaciones antes de considerarlas fallidas.

## Scheduler Pattern

Ejecuta tareas programadas para reprocesar eventos que no pudieron completarse correctamente.

Su objetivo es aumentar la resiliencia del sistema sin intervención manual.