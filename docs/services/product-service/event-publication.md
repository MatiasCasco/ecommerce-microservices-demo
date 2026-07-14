# Event Publication

## Objetivo

Product Service publica eventos de dominio cada vez que ocurre un cambio relevante sobre el catálogo de productos.

Estos eventos permiten que otros microservicios mantengan sus propias proyecciones locales sincronizadas sin depender de llamadas síncronas (REST).

Product Service únicamente comunica que un cambio ocurrió; nunca conoce quién consume sus eventos.

---

# Arquitectura

```text
               Product Service
                      │
                      ▼
             Domain Event
                      │
                      ▼
                 RabbitMQ
                      │
      ┌───────────────┼────────────────┐
      ▼               ▼                ▼
 Order Service   Notification      Future Services
                     Service
```

Esta arquitectura desacopla completamente al productor de los consumidores.

---

# Flujo de Publicación

```text
        Administrator
               │
               ▼
       Product Service
               │
               ▼
      Ejecutar Caso de Uso
               │
               ▼
      Persistir Información
               │
               ▼
      Construir Domain Event
               │
               ▼
      ProductEventPublisher
               │
               ▼
           RabbitMQ
               │
               ▼
          Consumidores
```

Los eventos se publican únicamente cuando la operación fue persistida exitosamente.

---

# Eventos Publicados

Actualmente Product Service publica los siguientes eventos:

| Evento | Descripción |
|---------|-------------|
| PRODUCT_CREATED | Se creó un nuevo producto. |
| PRODUCT_UPDATED | Se modificó la información general del producto. |
| PRODUCT_ACTIVATED | El producto pasó a estado ACTIVE. |
| PRODUCT_DEACTIVATED | El producto pasó a estado INACTIVE. |
| PRODUCT_STOCK_UPDATED | Se actualizó el stock disponible. |

Cada evento representa un cambio de negocio ocurrido sobre el catálogo.

---

# Contenido de los Eventos

Todos los eventos comparten una estructura común.

Información principal:

- eventId
- eventType
- eventVersion
- aggregateId
- traceId
- occurredAt

Cada evento incorpora además la información específica necesaria para que los consumidores actualicen sus proyecciones.

---

# Responsabilidad del ProductEventPublisher

ProductEventPublisher es responsable de:

- construir el mensaje final
- publicar el evento en RabbitMQ
- registrar logs estructurados
- propagar el TraceId

No contiene reglas de negocio.

La lógica del dominio permanece en Product Service.

---

# Consumidores

Product Service no conoce qué servicios consumen sus eventos.

Actualmente los eventos son utilizados por:

- Order Service

En futuras versiones podrán ser consumidos por:

- Notification Service
- Inventory Service
- Analytics Service
- Search Service
- Recommendation Service

La incorporación de nuevos consumidores no requiere modificaciones en Product Service.

---

# Principios de Diseño

La publicación de eventos sigue los siguientes principios:

## Domain Events

Los eventos representan hechos del negocio.

No representan llamadas remotas ni comandos.

Ejemplos:

✔ ProductCreated

✔ ProductUpdated

✘ UpdateOrder

---

## Bajo Acoplamiento

El productor nunca conoce quién consume el evento.

Únicamente comunica que un cambio ocurrió.

---

## Event-Driven Architecture

La comunicación entre microservicios se realiza mediante eventos.

Esto permite:

- independencia entre servicios
- escalabilidad
- resiliencia
- evolución independiente

---

## Consistencia Eventual

Los consumidores actualizan sus proyecciones utilizando los eventos recibidos.

Durante un breve período puede existir una diferencia entre la información publicada y la información almacenada por los consumidores.

Este comportamiento es esperado dentro de una arquitectura orientada a eventos.

---

# Logging y Observabilidad

Cada publicación genera un log estructurado que incluye:

- eventId
- eventType
- aggregateId
- traceId

Esto permite seguir un evento desde su publicación hasta su consumo en otros microservicios.

---

# Evolución

La estrategia de publicación evolucionará incorporando mecanismos de resiliencia y confiabilidad.

Próximas mejoras:

- Outbox Pattern
- Publisher Confirms
- Retry
- Dead Letter Queue (DLQ)
- Versionado de eventos
- Idempotencia
- OpenTelemetry

Estas mejoras permitirán garantizar una publicación de eventos más robusta y preparada para escenarios distribuidos.