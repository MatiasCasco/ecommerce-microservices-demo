# System Flow

## Objetivo

Este documento describe el flujo funcional del ecosistema **Ecommerce Microservices Demo**.

Su propósito es explicar cómo colaboran los microservicios durante la ejecución de un caso de negocio, desde la autenticación del usuario hasta la creación de una orden.

Los detalles internos de cada servicio se documentan en sus respectivos `*-flow.md`.

---

# Flujo General del Sistema

```text
                Cliente
                    │
                    ▼
         POST /auth/login
                    │
                    ▼
             User Service
                    │
             Validar Usuario
                    │
                    ▼
               Generar JWT
                    │
                    ▼
────────────────────────────────────────────────────

           GET /products
                    │
                    ▼
           Product Service
                    │
             Consultar Catálogo
                    │
                    ▼
          Lista de Productos
                    │
                    ▼
────────────────────────────────────────────────────

            POST /orders
                    │
                    ▼
            Order Service
                    │
                    ▼
       Consultar ProductCatalog
          (Proyección Local)
                    │
                    ▼
          Validar Productos
                    │
                    ▼
           Construir Orden
                    │
                    ▼
         Persistir Order
                    │
                    ▼
         Estado Inicial
        PENDING_PAYMENT
```

---

# Comunicación entre Servicios

El ecosistema combina comunicación síncrona y asíncrona.

## Comunicación REST

Utilizada cuando el cliente interactúa directamente con un microservicio.

Ejemplos:

- Registro de usuarios.
- Inicio de sesión.
- Consulta del catálogo.
- Creación de órdenes.

---

## Comunicación mediante Eventos

Utilizada para sincronizar información entre microservicios sin generar dependencias directas.

Actualmente:

```text
Product Service

↓

Persistir Producto

↓

Publicar Evento

↓

RabbitMQ

↓

Order Service

↓

Actualizar ProductCatalog
```

Los consumidores procesan los eventos de forma independiente.

---

# Modelo Operacional

Product Service representa el modelo operacional del sistema.

```text
Product Service

↓

PostgreSQL

↓

Información Actual
```

Contiene siempre el estado vigente de los productos.

---

# Modelo Histórico

Order Service representa el modelo histórico.

```text
Order Service

↓

Order

↓

OrderItems

↓

Snapshot Histórico
```

Cada orden conserva la información del producto tal como existía al momento de la compra.

---

# Proyección Local

Order Service mantiene una copia sincronizada del catálogo.

```text
Product Service

↓

RabbitMQ

↓

ProductCatalog

↓

Order Service
```

Esta proyección permite validar productos sin depender de llamadas REST.

---

# Estado Actual del Flujo

Actualmente el flujo completo finaliza con la creación de una orden.

```text
Login

↓

Consultar Catálogo

↓

Crear Orden

↓

PENDING_PAYMENT
```

---

# Evolución del Flujo

La arquitectura fue diseñada para crecer de manera incremental.

El flujo evolucionará incorporando nuevos dominios.

```text
Login

↓

Consultar Productos

↓

Crear Orden

↓

Confirmar Pago

↓

Reservar Stock

↓

Actualizar Inventario

↓

Enviar Notificación

↓

Preparar Envío

↓

Despachar Pedido

↓

Entrega Confirmada
```

Cada nueva etapa será implementada mediante nuevos microservicios y comunicación basada en eventos.

---

# Documentación Relacionada

Para comprender cada parte del flujo consultar:

## Arquitectura

- overview.md
- rabbitmq.md
- event-model.md
- projections.md

## Servicios

- user-service/authentication-flow.md
- product-service/product-flow.md
- order-service/order-flow.md