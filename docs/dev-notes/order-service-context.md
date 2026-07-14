# Ecommerce Microservices Demo - Development Context

## Objetivo de este documento

Este documento sirve como contexto rápido para continuar el desarrollo del proyecto desde cualquier conversación.

No es documentación funcional del sistema.

Es una guía para que cualquier IA o desarrollador pueda entender rápidamente:

- estado actual del proyecto
- decisiones arquitectónicas ya tomadas
- principios de diseño
- próximos objetivos

---

# Proyecto

Nombre:

ecommerce-microservices-demo

Arquitectura:

Microservicios

Stack:

- Java 21
- Spring Boot 3
- Spring Security
- JWT
- RabbitMQ
- PostgreSQL
- Spring Data JPA
- Docker
- Maven
- Log4j2

Servicios:

- api-gateway
- common-lib
- product-service
- order-service
- notification-service

---

# Filosofía del proyecto

Este proyecto NO busca únicamente implementar funcionalidades.

El objetivo principal es aprender y demostrar buenas prácticas utilizadas en sistemas reales.

Cada decisión debe estar respaldada por una necesidad del negocio.

Siempre seguimos este proceso:

Negocio

↓

Reglas del negocio

↓

Modelo del dominio

↓

Implementación

Nunca diseñamos primero el código.

---

# Estado actual

## common-lib

Implementado:

- Logging estructurado (CommerceLog)
- TraceId
- Excepciones compartidas
- Eventos compartidos
- JWT Utilities

---

## product-service

Estado:

Completamente funcional.

Implementado:

- CRUD Productos
- CRUD Categorías
- Specification API
- Pageable
- Sorting
- Seguridad
- RabbitMQ Producer
- Logging estructurado
- TraceId

Eventos publicados:

- PRODUCT_CREATED
- PRODUCT_UPDATED
- PRODUCT_ACTIVATED
- PRODUCT_DEACTIVATED
- PRODUCT_STOCK_UPDATED

---

## order-service

Implementado:

- Base espejo (catalog_products)
- RabbitMQ Consumer
- Sincronización del catálogo
- ProductCatalogService
- Logging
- Observabilidad

Actualmente catalog_products representa una proyección local sincronizada mediante eventos provenientes de Product Service.

Order Service NO consulta Product Service mediante REST.

Toda la información necesaria para crear órdenes se obtiene desde catalog_products.

---

# Decisiones Arquitectónicas

## Local Projection

Order Service mantiene una copia local del catálogo.

Motivos:

- reducir acoplamiento
- mejorar disponibilidad
- evitar llamadas síncronas
- disminuir latencia
- prepararse para Event Driven Architecture

---

## Event Driven

Product Service es el productor.

Order Service consume eventos.

Actualmente existen eventos para:

- PRODUCT_CREATED
- PRODUCT_UPDATED
- PRODUCT_ACTIVATED
- PRODUCT_DEACTIVATED
- PRODUCT_STOCK_UPDATED

---

## Modelo Operacional vs Modelo Histórico

Una de las decisiones más importantes del proyecto.

Product Service representa el modelo operacional.

Siempre contiene el estado actual del producto.

Order Service representa el modelo histórico.

Una orden conserva la información existente al momento de la compra.

Aunque el producto cambie posteriormente.

---

# Dominio descubierto

## Order

Una Order representa una compra.

No representa el catálogo.

No depende del estado actual de Product Service.

Una Order funciona como un Snapshot del momento de la compra.

---

## OrderItem

Cada OrderItem representa una línea de compra.

Debe conservar:

- productId
- productName
- unitPrice
- quantity
- subtotal

No consulta nuevamente el catálogo.

Debe comportarse como un registro histórico.

---

## Aggregate Root

Order será el Aggregate Root.

OrderItem nunca será manipulado directamente.

Toda modificación ocurre a través de Order.

---

# Estados iniciales

Actualmente se plantea comenzar con:

- PENDING_PAYMENT
- PAID
- CANCELLED

Las órdenes nunca se eliminan.

Se mantienen por motivos de:

- auditoría
- métricas
- trazabilidad

---

# Flujo de creación de órdenes

POST /orders

↓

Validar Request

↓

Consultar catalog_products

↓

Validar:

- producto existente
- producto activo
- stock suficiente

↓

Construir OrderItems

↓

Calcular subtotales

↓

Calcular total

↓

Persistir Order

↓

Persistir OrderItems

↓

Estado:

PENDING_PAYMENT

---

# Principios del dominio

El cliente nunca envía:

- precio
- subtotal
- total

El servidor siempre calcula:

- subtotal
- total

Order conoce su total.

OrderItem conoce su subtotal.

Los Services únicamente orquestan.

---

# Discusión sobre Inventario

Se analizaron dos estrategias.

## Opción A (Elegida para evolución futura)

Reservar stock.

Ventajas:

- mejor experiencia para el cliente
- evita overselling
- similar a Amazon

Actualmente NO será implementada porque:

catalog_products es una proyección de lectura.

Product Service continúa siendo el dueño del inventario.

---

## Opción B

Descontar stock luego del pago.

Más sencilla.

No será el objetivo final.

---

# Estado actual del roadmap

Completado

- Product Service
- RabbitMQ Producer
- Local Projection
- RabbitMQ Consumer
- Sincronización del catálogo
- Logging
- Observabilidad

En desarrollo

Diseño del dominio Order.

Pendiente

- Aggregate Order
- Aggregate OrderItem
- DTOs
- OrderService
- Create Order
- ORDER_CREATED Event

Futuro

- Payment Service
- Inventory Service
- Reserva de Stock
- Notification Service
- Retry
- DLQ
- Publisher Confirms
- Outbox Pattern
- Saga Pattern
- Idempotencia
- Observabilidad distribuida

---

# Forma de trabajo

Actuar como Tech Lead.

Antes de escribir código:

- analizar el negocio
- justificar las decisiones
- identificar ventajas y desventajas
- mantener consistencia con la arquitectura
- evitar sobreingeniería

La prioridad no es escribir código rápido.

La prioridad es construir un sistema que pueda evolucionar de manera ordenada.