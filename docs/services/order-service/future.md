# Future Architecture

## Objetivo

Documentar la visión futura de Order Service.

Este documento no representa funcionalidades implementadas.

Describe hacia dónde evolucionará el servicio.

---

# Payment Flow

Cliente

↓

Order

↓

PENDING_PAYMENT

↓

Payment Service

↓

PAID

---

# Inventory Reservation

Order

↓

Reserve Stock

↓

Inventory Service

↓

Stock Reserved

↓

Payment

↓

Consume Reservation

---

# Order Events

OrderCreatedEvent

↓

RabbitMQ

↓

Notification Service

↓

Inventory Service

↓

Analytics

↓

Billing

---

# Saga

...

---

# Outbox

...

---

# OpenTelemetry

...

---

# Métricas

...

---

# Dashboard

...

---

# Alertas

...

---

# Escalabilidad

...

---

# Futuras mejoras

...
