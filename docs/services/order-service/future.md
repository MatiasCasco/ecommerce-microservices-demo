# Future Architecture

## Objetivo

Este documento describe la visión futura de Order Service.

No representa funcionalidades implementadas.

Su propósito es documentar las capacidades hacia las que puede evolucionar la arquitectura, manteniendo las responsabilidades separadas y evitando incorporar complejidad antes de que exista una necesidad concreta.

El roadmap define el orden de evolución.

Este documento describe principalmente la dirección arquitectónica.

---

# Evolución General

La evolución prevista parte del modelo actual:

```text
Order Service
      │
      ├── Order
      ├── ProductCatalog
      └── Product Event Consumer
```

y puede evolucionar hacia:

```text
                         Order Service
                              │
                    ┌─────────┴─────────┐
                    │                   │
                 Order             ProductCatalog
                    │                   │
                    ▼                   ▼
             Order Events        Product Events
                    │                   │
                    ▼                   ▼
                 RabbitMQ          RabbitMQ
                    │
          ┌─────────┼──────────┐
          ▼         ▼          ▼
      Payment   Inventory  Notification
```

El objetivo es mantener el desacoplamiento entre los servicios.

---

# Order Events

Actualmente Order Service consume eventos de Product Service.

Una evolución posterior permitirá que Order Service publique sus propios eventos.

El primer evento previsto es:

```text
ORDER_CREATED
```

Flujo conceptual:

```text
Order Service
      │
      │ ORDER_CREATED
      ▼
   RabbitMQ
      │
      ├──────────────► Notification Service
      │
      ├──────────────► Payment Service
      │
      ├──────────────► Inventory Service
      │
      └──────────────► Analytics
```

Los consumidores podrán reaccionar al evento sin que Order Service conozca directamente sus implementaciones.

Order Service no debería evolucionar hacia llamadas síncronas directas como:

```text
Order → Notification
Order → Payment
Order → Inventory
```

porque eso volvería a introducir acoplamiento entre servicios.

---

# Payment Service

Actualmente una Order comienza en:

```text
PENDING_PAYMENT
```

El pago será una responsabilidad independiente.

Evolución conceptual:

```text
Order
  │
  ▼
PENDING_PAYMENT
  │
  ▼
Payment Service
  │
  ├── payment success
  │
  ▼
PaymentConfirmedEvent
  │
  ▼
Order Service
  │
  ▼
PAID
```

La transición a `PAID` representa un pago confirmado.

Por este motivo, `ADMIN` no debe marcar manualmente una Order como `PAID`.

La operación de pago pertenece al contexto de Payment.

---

# Inventory / Reservation

Actualmente `ProductCatalog.availableStock` representa el stock conocido por Order Service.

No representa una reserva.

```text
Order CREATED
      ≠
Stock RESERVED
```

La evolución prevista introduce una responsabilidad específica para Inventory:

```text
Order
   │
   ▼
Reserve Stock
   │
   ▼
Inventory Service
   │
   ▼
Stock Reserved
```

Inventory podrá evolucionar hacia operaciones como:

```text
reserve
confirm
release
```

Esto permitirá abordar problemas que no deben resolverse dentro de `Order`, como:

- concurrencia;
- overselling;
- reserva;
- liberación de stock.

La coordinación entre Order, Payment e Inventory podrá evolucionar posteriormente hacia una Saga.

---

# Saga

Cuando existan varios procesos distribuidos que deban coordinarse, la arquitectura podrá evolucionar hacia una Saga.

Conceptualmente:

```text
Create Order
     │
     ▼
Reserve Stock
     │
     ▼
Process Payment
     │
     ├── success
     │
     └── failure
            │
            ▼
      Compensating Action
```

La Saga no forma parte del MVP.

No debe implementarse únicamente por anticipación.

Primero debe existir una necesidad real de coordinar transacciones distribuidas.

---

# Outbox Pattern

Cuando Order Service comience a persistir una Order y publicar eventos propios aparecerá un problema de consistencia:

```text
Order persisted
      +
Order event NOT published
```

La evolución prevista para resolver este problema es:

```text
Order Transaction
      │
      ├── Order
      │
      └── Outbox Event
              │
              ▼
          Publisher
              │
              ▼
           RabbitMQ
```

El Outbox Pattern permitirá reducir la posibilidad de que la persistencia y publicación queden en estados diferentes.

No forma parte del MVP.

---

# Idempotencia

La arquitectura deberá evolucionar hacia consumidores idempotentes cuando aumente el uso de mensajería.

Un mismo evento podría ser recibido más de una vez:

```text
ORDER_CREATED
      │
      ├── delivery 1
      └── delivery 2
```

El procesamiento repetido no debería producir efectos de negocio incorrectos.

La idempotencia se aplicará donde el flujo distribuido lo requiera.

---

# Retry y DLQ

La mensajería podrá evolucionar para incorporar:

```text
Consumer
   │
   ▼
Processing
   │
   ├── success
   │
   └── failure
          │
          ▼
        Retry
          │
          ├── success
          │
          └── failure
                 │
                 ▼
                 DLQ
```

Esto permitirá separar:

- errores transitorios;
- errores permanentes;
- mensajes que requieren intervención o análisis.

La implementación debe realizarse cuando el consumo de eventos propios y su criticidad lo justifiquen.

---

# Publisher Confirms

Cuando Order Service publique eventos propios, podrá incorporarse confirmación del broker para aumentar la confiabilidad de la publicación.

Conceptualmente:

```text
Order Service
      │
      ▼
RabbitMQ
      │
      ▼
Publisher Confirmation
```

Esta capacidad se relaciona posteriormente con la estrategia de publicación y con Outbox.

---

# Customer Projection

Actualmente no se implementa una `CustomerProjection`.

El servicio utiliza el `customerId` necesario para el caso de uso y las reglas de ownership.

Una evolución futura podría introducir una proyección local:

```text
Customer Service
      │
      │ Customer Events
      ▼
CustomerProjection
      │
      ▼
Order Service
```

Esto podría permitir:

- validar existencia local del Customer;
- evitar dependencias REST;
- consultar información necesaria para nuevos casos de uso;
- mantener el desacoplamiento.

No debe crearse mientras no exista una necesidad concreta que lo justifique.

---

# API Gateway

La arquitectura puede evolucionar hacia una frontera:

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Order Service
```

El Gateway podrá concentrar responsabilidades transversales como:

- routing;
- entrada única;
- autenticación/validación JWT según la estrategia adoptada;
- rate limiting;
- headers;
- observabilidad;
- políticas transversales.

Order Service debe continuar siendo responsable de la autorización propia de sus casos de uso.

La separación permanece:

```text
Gateway
   ↓
¿La petición está autenticada?

Order Service
   ↓
¿Este actor puede ejecutar el caso de uso?

Order
   ↓
¿La operación es válida según las reglas del negocio?
```

---

# Observabilidad

La arquitectura puede evolucionar hacia observabilidad distribuida.

La dirección prevista incluye:

- OpenTelemetry;
- trazabilidad distribuida;
- métricas;
- dashboards;
- alertas.

Conceptualmente:

```text
Order Service
      │
      ├── Logs
      ├── Metrics
      └── Traces
             │
             ▼
       Observability
```

El `TraceId` existente proporciona una base para continuar esta evolución.

---

# Escalabilidad

A medida que aumente la carga, Order Service podrá evolucionar mediante:

- escalado horizontal;
- procesamiento asíncrono;
- separación de responsabilidades;
- optimización de consultas;
- métricas para identificar cuellos de botella.

La escalabilidad no debe utilizarse como justificación para introducir infraestructura innecesaria antes de conocer el comportamiento real del sistema.

---

# Principio de Evolución

Las futuras capacidades deben incorporarse como respuesta a problemas reales.

```text
Problema
   ↓
Análisis
   ↓
Opciones
   ↓
Trade-offs
   ↓
Decisión
   ↓
Implementación
   ↓
Evolución
```

La arquitectura actual debe dejar puntos de extensión razonables sin implementar prematuramente:

- Payment;
- Inventory;
- Reservation;
- Order Events;
- Customer Projection;
- Retry;
- DLQ;
- Publisher Confirms;
- Outbox;
- Saga;
- Idempotencia;
- Observabilidad distribuida.

La regla general es:

> Diseñar para evolucionar, pero implementar solamente lo que necesita el negocio actual.
