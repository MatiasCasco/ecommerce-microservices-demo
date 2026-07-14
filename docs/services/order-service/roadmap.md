# Order Service Roadmap

## Objetivo

Este documento describe la evolución planificada de Order Service.

Cada fase construye sobre la anterior.

La prioridad es mantener una arquitectura simple, consistente y evolutiva.

---

# Estado Actual

## Implementado

- [x] Product Catalog Projection
- [x] RabbitMQ Consumer
- [x] ProductCatalogService
- [x] Sincronización mediante eventos
- [x] Logging estructurado
- [x] TraceId
- [x] Base espejo sincronizada

---

# Fase 1

## Dominio

- [ ] Order Aggregate
- [ ] OrderItem Aggregate
- [ ] OrderStatus

---

# Fase 2

## Caso de uso

- [ ] Create Order
- [ ] Validaciones
- [ ] Persistencia
- [ ] DTOs
- [ ] API REST

---

# Fase 3

## Eventos

- [ ] OrderCreatedEvent
- [ ] RabbitMQ Producer
- [ ] Publicación de eventos

---

# Fase 4

## Notification Service

- [ ] Consumo de OrderCreatedEvent
- [ ] Envío de notificaciones

---

# Fase 5

## Payment

- [ ] Payment Service
- [ ] Confirmación de pago
- [ ] Cambio de estado

---

# Fase 6

## Inventario

- [ ] Reserva de stock
- [ ] Liberación de reservas
- [ ] Consumo de reservas

---

# Fase 7

## Resiliencia

- [ ] Retry
- [ ] Dead Letter Queue
- [ ] Publisher Confirms
- [ ] Consumer Retry

---

# Fase 8

## Arquitectura

- [ ] Outbox Pattern
- [ ] Saga Pattern
- [ ] Idempotencia
- [ ] Observabilidad distribuida