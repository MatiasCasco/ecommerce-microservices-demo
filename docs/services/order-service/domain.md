# Order Service Domain

## Objetivo

Este documento describe el dominio funcional de Order Service.

No pretende explicar detalles de implementación.

Su objetivo es documentar las reglas de negocio y las decisiones de diseño que dieron origen al modelo del dominio.

---

# ¿Qué representa una Order?

Una Order representa el compromiso de compra realizado por un cliente.

No representa el catálogo.

No representa el estado actual de los productos.

Representa una fotografía (Snapshot) del momento en que la compra fue realizada.

Una vez creada, la orden debe conservar toda la información necesaria para reconstruir la compra, incluso si el catálogo cambia posteriormente.

---

# Modelo Operacional vs Modelo Histórico

Uno de los principios fundamentales del proyecto es separar ambos modelos.

## Product Service

Representa el modelo operacional.

Siempre refleja el estado actual de los productos.

Ejemplo:

- nombre actual
- precio actual
- stock actual
- estado actual

---

## Order Service

Representa el modelo histórico.

Cada orden conserva:

- producto comprado
- precio pagado
- cantidad comprada
- subtotal
- total

La orden nunca depende nuevamente del catálogo para reconstruir una compra.

---

# Snapshot

Una orden funciona como un Snapshot del catálogo.

Ejemplo:

Día 1

Notebook Lenovo

Precio:

Gs. 4.500.000

↓

Cliente compra.

↓

Se crea la Order.

↓

Día 10

El administrador modifica:

Notebook Lenovo LOQ Gen 10

Precio:

Gs. 5.300.000

↓

La orden continúa mostrando:

Notebook Lenovo

Gs. 4.500.000

porque representa el estado existente al momento de la compra.

---

# Aggregate Root

Order es el Aggregate Root del dominio.

Toda modificación del agregado ocurre a través de Order.

OrderItem nunca será manipulado de forma independiente.

Responsabilidades de Order:

- administrar OrderItems
- calcular el total
- controlar el estado
- garantizar la consistencia del agregado

---

# OrderItem

OrderItem representa una línea de compra.

Cada OrderItem conserva información histórica.

Campos principales:

- productId
- productName
- unitPrice
- quantity
- subtotal

Su responsabilidad es representar exactamente lo que el cliente compró.

---

# Responsabilidades del Dominio

El dominio conoce sus propias reglas.

Order conoce:

- cómo agregar Items
- cómo eliminar Items
- cómo calcular el total

OrderItem conoce:

- cómo calcular su subtotal

Los Services únicamente coordinan el caso de uso.

---

# Estados de la Orden

Estados iniciales definidos:

- PENDING_PAYMENT
- PAID
- CANCELLED

Las órdenes nunca son eliminadas.

Se mantienen para:

- auditoría
- trazabilidad
- métricas
- análisis del negocio

---

# Principios del Request

El cliente nunca define:

- precio
- subtotal
- total

El cliente únicamente informa:

- customerId
- productos
- cantidades

Todos los importes son calculados por el servidor.

---

# Principios del Dominio

Durante el diseño se adoptaron los siguientes principios.

## Single Source of Truth

Product Service continúa siendo el dueño del catálogo.

Order Service únicamente mantiene una proyección local sincronizada mediante eventos.

---

## Bajo Acoplamiento

Order Service nunca consulta Product Service durante la creación de una orden.

Toda la validación ocurre utilizando ProductCatalog.

---

## Consistencia

La creación de la orden ocurre dentro de una única transacción.

No pueden existir órdenes parcialmente persistidas.

---

## Inmutabilidad del Historial

Una vez creada una orden:

- el nombre del producto no cambia
- el precio no cambia
- la cantidad no cambia
- el subtotal no cambia

La orden representa un documento histórico.

---

# Evolución del Dominio

El modelo fue diseñado para evolucionar gradualmente.

Próximas etapas:

- Payment Service
- Inventory Reservation
- Order Events
- Notification Service
- Saga Pattern
- Outbox Pattern
- Idempotencia
- Resiliencia

Cada nueva funcionalidad deberá respetar los principios establecidos en este documento.