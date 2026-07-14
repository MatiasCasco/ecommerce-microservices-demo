# Order Creation Flow

## Objetivo

Este documento describe el flujo de creación de una orden dentro de **Order Service**.

Actualmente el servicio utiliza una **proyección local del catálogo de productos** (`ProductCatalog`) sincronizada mediante eventos provenientes de **Product Service**.

Esto elimina la necesidad de realizar llamadas REST durante el proceso de compra.

---

# Flujo Actual

```text
                Cliente
                    │
                    ▼
             POST /orders
                    │
                    ▼
             Order Controller
                    │
                    ▼
              Order Service
                    │
                    ▼
       Consultar ProductCatalog
                    │
                    ▼
         Validar Productos
                    │
      ┌─────────────┼──────────────┐
      │             │              │
      ▼             ▼              ▼
 ¿Existe?     ¿Está ACTIVE?   ¿Stock suficiente?
      │             │              │
      └─────────────┴──────────────┘
                    │
                    ▼
        Construir Aggregate Order
                    │
                    ▼
      Construir OrderItems
                    │
                    ▼
      Calcular Subtotales
                    │
                    ▼
        Calcular Total
                    │
                    ▼
      Persistir Order + Items
                    │
                    ▼
          Estado Inicial
      PENDING_PAYMENT
                    │
                    ▼
             HTTP 201 Created
```

---

# Descripción del Flujo

## 1. Recepción de la solicitud

El cliente envía una solicitud para crear una nueva orden.

El Request únicamente contiene:

- customerId
- productos
- cantidades

El cliente nunca envía:

- precio
- subtotal
- total

Estos valores siempre son calculados por el servidor.

---

## 2. Consulta del catálogo local

Order Service consulta `ProductCatalog`.

No realiza llamadas REST hacia Product Service.

El catálogo local representa una proyección sincronizada mediante RabbitMQ.

---

## 3. Validaciones

Para cada producto se verifica:

- existencia
- estado ACTIVE
- stock suficiente

Si alguna validación falla, la orden no se crea.

---

## 4. Construcción del Aggregate

Se construye el Aggregate Root `Order`.

Cada producto genera un `OrderItem` que representa una fotografía del producto al momento de la compra.

Cada OrderItem conserva:

- productId
- productName
- unitPrice
- quantity
- subtotal

---

## 5. Cálculo de importes

Cada OrderItem calcula su propio subtotal.

Posteriormente Order calcula el total de la compra.

La lógica de negocio pertenece al dominio y no al Service.

---

## 6. Persistencia

Order y OrderItems se persisten dentro de una única transacción.

Si ocurre cualquier error, toda la operación es revertida.

---

## 7. Estado inicial

Toda nueva orden comienza con el estado:

```text
PENDING_PAYMENT
```

La orden nunca nace pagada.

---

# Evolución del flujo

En las próximas iteraciones el flujo evolucionará hacia un proceso completamente orientado a eventos.

```text
Cliente
    │
    ▼
POST /orders
    │
    ▼
Persistir Order
    │
    ▼
OrderCreatedEvent
    │
    ▼
RabbitMQ
    │
    ├────────► Notification Service
    │
    ├────────► Payment Service
    │
    └────────► Inventory Service
```

La publicación de eventos será implementada una vez finalizado el caso de uso principal de creación de órdenes.

---

# Principios de diseño

Este flujo fue diseñado siguiendo los siguientes principios:

- Domain-Driven Design (DDD)
- Aggregate Root
- Event-Driven Architecture (EDA)
- Local Projection Pattern
- Transactional Consistency
- Bajo acoplamiento entre microservicios