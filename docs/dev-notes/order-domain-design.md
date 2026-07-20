# Order Service - Domain Design Notes

> Estado: Diseño
>
> Este documento captura las decisiones de diseño del dominio antes de su implementación.
>
> Su objetivo es preservar el razonamiento arquitectónico para evitar perder contexto durante el desarrollo.

---

# Objetivo

Antes de escribir una línea de código queremos comprender completamente el dominio.

La implementación debe ser una consecuencia del diseño y no al revés.

---

# Diseño del Dominio

El dominio de Order Service estará compuesto por los siguientes conceptos.

```text
Order

↓

OrderItem

↓

OrderStatus

↓

ProductCatalog

↓

Order Aggregate
```

Cada uno deberá diseñarse individualmente antes de comenzar la implementación.

---

# Order

## Preguntas que debemos responder

- ¿Qué representa una Order?
- ¿Cuál es su responsabilidad?
- ¿Cuáles son sus invariantes?
- ¿Qué estados puede tener?
- ¿Qué información debe persistir?
- ¿Qué comportamiento pertenece a Order y no al Service?
- ¿Qué operaciones del negocio puede ejecutar?

Antes de implementar la entidad debemos comprender completamente estas reglas.

---

# OrderItem

## Preguntas que debemos responder

- ¿Qué representa un OrderItem?
- ¿Debe ser mutable o inmutable?
- ¿Cómo calcula su subtotal?
- ¿Qué información copia desde ProductCatalog?
- ¿Qué información nunca debe modificarse?
- ¿Por qué representa un Snapshot histórico?

Actualmente la decisión arquitectónica es que OrderItem represente el estado del producto al momento de la compra.

---

# OrderStatus

## Preguntas que debemos responder

- ¿Qué estados existen?
- ¿Qué transiciones son válidas?
- ¿Quién puede cambiar el estado?
- ¿Qué eventos del negocio producen esos cambios?

Estados previstos:

```text
PENDING_PAYMENT

↓

PAID

↓

PROCESSING

↓

SHIPPED

↓

DELIVERED
```

También deberán contemplarse estados alternativos como:

- CANCELLED
- PAYMENT_FAILED
- REFUNDED

---

# ProductCatalog

## Preguntas que debemos responder

- ¿Por qué existe?
- ¿Qué información mantiene?
- ¿Qué información NO debe mantener?
- ¿Cómo se sincroniza?
- ¿Qué eventos consume?
- ¿Por qué es una proyección y no una consulta REST?

Recordar siempre:

ProductCatalog NO representa el catálogo oficial.

Es únicamente una proyección local sincronizada mediante eventos.

---

# Order Aggregate

## Preguntas que debemos responder

- ¿Por qué Order es el Aggregate Root?
- ¿Qué objetos pertenecen al Aggregate?
- ¿Qué reglas deben mantenerse consistentes?
- ¿Qué operaciones nunca deben ejecutarse fuera del Aggregate?

El objetivo es aplicar correctamente los principios de Domain-Driven Design.

---

# Decisiones ya tomadas

Hasta el momento se acordó:

- Product Service es el Single Source of Truth del catálogo.
- Order Service mantiene una proyección local.
- Order representa el modelo histórico.
- OrderItem conserva un Snapshot del producto.
- El historial nunca debe modificarse.
- El stock no se obtiene mediante llamadas REST.
- ProductCatalog se sincroniza mediante RabbitMQ.
- La orden comienza en estado PENDING_PAYMENT.
- El dominio debe contener las reglas del negocio.
- Los Services únicamente coordinan los casos de uso.

---

# Diseño antes que código

La implementación seguirá el siguiente orden.

```text
Comprender el negocio

↓

Diseñar el dominio

↓

Definir responsabilidades

↓

Definir invariantes

↓

Diseñar el Aggregate

↓

Diseñar entidades

↓

Diseñar Value Objects (si aplica)

↓

Recién entonces escribir código
```

---

# Próximas sesiones

Antes de implementar continuaremos diseñando:

- Order
- OrderItem
- OrderStatus
- ProductCatalog
- Aggregate
- Casos de uso
- Eventos futuros
- Integración con Payment
- Integración con Inventory

---

# Filosofía

No queremos escribir entidades anémicas.

Queremos construir un dominio rico donde las reglas del negocio vivan dentro del dominio y no dispersas en los Services.

El código debe ser la consecuencia del diseño, nunca el punto de partida.