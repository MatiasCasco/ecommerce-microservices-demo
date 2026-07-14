# ADR-001: Use RabbitMQ for Asynchronous Communication

## Status

Accepted

---

## Context

La arquitectura evolucionó desde una comunicación principalmente síncrona entre microservicios hacia un enfoque orientado a eventos.

Era necesario desacoplar los servicios para reducir dependencias directas, evitar llamadas REST innecesarias y permitir que cada servicio evolucionara de forma independiente.

Además, ORDER SERVICE necesita mantener información del catálogo de productos sin depender continuamente de PRODUCT SERVICE durante la creación de una orden.

---

## Decision

Se utilizará RabbitMQ como broker de mensajería para distribuir eventos de dominio entre los microservicios.

### Product Service

- Es el **Source of Truth** del catálogo de productos.
- Publica eventos cuando un producto cambia de estado.

Eventos publicados:

- ProductCreatedEvent
- ProductUpdatedEvent
- ProductActivatedEvent
- ProductDeactivatedEvent
- ProductStockUpdatedEvent

### Order Service

- Consume los eventos publicados por PRODUCT SERVICE.
- Mantiene una proyección local (`ProductCatalog`) sincronizada mediante eventos.
- Utiliza esa información para validar órdenes sin realizar consultas REST al PRODUCT SERVICE.

### Notification Service

- Consume eventos para generar notificaciones.
- Persiste la información en MongoDB.
- Reintenta el procesamiento mediante un scheduler cuando ocurre un fallo.

---

## Consequences

### Positivas

- Menor acoplamiento entre microservicios.
- Eliminación de consultas REST entre ORDER SERVICE y PRODUCT SERVICE para validar productos.
- Mayor escalabilidad.
- Mayor resiliencia frente a fallos temporales.
- Cada servicio puede evolucionar de forma independiente.
- Mejor rendimiento al disponer de una copia local del catálogo.

### Negativas

- Consistencia eventual entre servicios.
- Mayor complejidad operacional.
- Necesidad de sincronizar correctamente los eventos.
- Requiere monitoreo de colas y consumidores.