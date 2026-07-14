# Product Flow

## Objetivo

Este documento describe los principales flujos funcionales implementados por **Product Service**.

Product Service es responsable de administrar el catálogo de productos y publicar los eventos de dominio que permiten mantener sincronizados los demás microservicios.

---

# Flujo de Creación de Producto

```text
             Administrator
                    │
                    ▼
          POST /products
                    │
                    ▼
          Product Controller
                    │
                    ▼
           Product Service
                    │
                    ▼
          Validar Request
                    │
                    ▼
      Construir Aggregate Product
                    │
                    ▼
        Persistir Producto
                    │
                    ▼
     Publicar ProductCreatedEvent
                    │
                    ▼
               RabbitMQ
                    │
                    ▼
           Microservicios
```

---

## Descripción

Durante la creación de un producto se realizan las siguientes acciones:

- Validar la solicitud.
- Verificar reglas de negocio.
- Construir el dominio Product.
- Persistir la información.
- Publicar un evento de dominio notificando la creación del producto.

---

# Flujo de Actualización

```text
             Administrator
                    │
                    ▼
          PUT /products/{id}
                    │
                    ▼
           Product Service
                    │
                    ▼
         Obtener Producto
                    │
                    ▼
        Validar existencia
                    │
                    ▼
     Actualizar información
                    │
                    ▼
         Persistir cambios
                    │
                    ▼
     Publicar ProductUpdatedEvent
```

---

## Información actualizada

Actualmente este flujo actualiza:

- nombre
- descripción
- precio
- categoría

El stock se administra mediante un flujo independiente.

---

# Flujo de Actualización de Stock

```text
             Administrator
                    │
                    ▼
     PATCH /products/{id}/stock
                    │
                    ▼
           Product Service
                    │
                    ▼
         Obtener Producto
                    │
                    ▼
      Validar nueva cantidad
                    │
                    ▼
        Actualizar Stock
                    │
                    ▼
         Persistir cambios
                    │
                    ▼
 ProductStockUpdatedEvent
                    │
                    ▼
               RabbitMQ
```

---

## Justificación

El stock posee un flujo independiente porque representa una responsabilidad distinta dentro del dominio.

Esto evita modificar accidentalmente el inventario durante una actualización general del producto.

---

# Flujo de Activación

```text
             Administrator
                    │
                    ▼
PATCH /products/{id}/activate
                    │
                    ▼
           Product Service
                    │
                    ▼
      Cambiar estado ACTIVE
                    │
                    ▼
         Persistir cambios
                    │
                    ▼
 ProductActivatedEvent
```

---

# Flujo de Desactivación

```text
             Administrator
                    │
                    ▼
PATCH /products/{id}/deactivate
                    │
                    ▼
           Product Service
                    │
                    ▼
    Cambiar estado INACTIVE
                    │
                    ▼
         Persistir cambios
                    │
                    ▼
ProductDeactivatedEvent
```

---

# Consulta de Productos

```text
            Cliente
                │
                ▼
        GET /products
                │
                ▼
       Product Service
                │
                ▼
Specification API
                │
                ▼
Pageable
                │
                ▼
Sorting
                │
                ▼
      Respuesta
```

Las consultas soportan:

- filtros dinámicos
- paginación
- ordenamiento

---

# Publicación de Eventos

Todo cambio relevante sobre Product genera un evento de dominio.

```text
Product Service
        │
        ▼
 Domain Event
        │
        ▼
   RabbitMQ
        │
        ├────────► Order Service
        │
        ├────────► Notification Service
        │
        └────────► Futuros consumidores
```

Product Service no conoce quién consume estos eventos.

Únicamente publica los cambios ocurridos en el dominio.

---

# Principios del Flujo

Todos los casos de uso siguen los mismos principios:

- Validar antes de modificar.
- Persistir dentro de una transacción.
- Publicar eventos únicamente después de una operación exitosa.
- Mantener Product Service como la única fuente oficial del catálogo.
- Desacoplar la comunicación mediante eventos.

---

# Evolución

En futuras iteraciones el flujo evolucionará incorporando:

- Outbox Pattern.
- Publisher Confirms.
- Retry.
- Dead Letter Queue.
- Versionado de eventos.
- Auditoría de cambios.