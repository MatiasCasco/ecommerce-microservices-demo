# Order Service API

## Objetivo

Este documento describe la API pública de **Order Service**.

Su propósito es explicar el comportamiento funcional de cada endpoint, las reglas de negocio aplicadas y las operaciones disponibles sobre el dominio de órdenes.

La especificación técnica completa (OpenAPI/Swagger) representa la fuente oficial para los contratos HTTP.

---

# Base Path

```
/orders
```

---

# Recursos

Order Service administra el ciclo de vida completo de las órdenes.

Las operaciones permiten:

- Crear órdenes.
- Consultar órdenes.
- Consultar historial.
- Gestionar el estado de una orden.

---

# Endpoints

## Crear Orden

### POST /orders

Crea una nueva orden de compra.

### Request

El cliente únicamente envía:

- customerId
- productos
- cantidades

El cliente nunca envía:

- precio
- subtotal
- total

Toda la información económica es obtenida desde `ProductCatalog`.

---

### Flujo

Durante la creación de una orden el servicio realiza las siguientes acciones:

- Validar la solicitud.
- Consultar ProductCatalog.
- Verificar que todos los productos existan.
- Verificar que los productos estén activos.
- Verificar disponibilidad de stock.
- Construir el Aggregate `Order`.
- Construir los `OrderItem`.
- Calcular subtotales.
- Calcular el total.
- Persistir la orden.
- Inicializar el estado en `PENDING_PAYMENT`.

---

### Estado inicial

```
PENDING_PAYMENT
```

---

### Eventos

Versión actual

- No publica eventos.

Versión futura

```
ORDER_CREATED
```

---

## Obtener Orden

### GET /orders/{id}

Obtiene el detalle completo de una orden.

La información devuelta representa un Snapshot histórico de la compra.

No consulta Product Service.

Toda la información proviene de la propia orden.

---

## Listar Órdenes

### GET /orders

Obtiene un listado paginado de órdenes.

### Características

- Paginación.
- Ordenamiento.
- Filtros (futuro).

---

## Cancelar Orden

### PATCH /orders/{id}/cancel

**Estado:** Futuro.

Permite cancelar una orden siempre que las reglas del negocio lo permitan.

---

# Validaciones

Durante la creación de una orden se validará:

- existencia del producto
- estado ACTIVE
- stock suficiente
- cantidades válidas

Si alguna validación falla, la operación será cancelada.

---

# ProductCatalog

Order Service nunca consulta Product Service mediante REST.

Toda la validación se realiza utilizando la proyección local del catálogo (`ProductCatalog`), sincronizada mediante eventos.

---

# Estados de la Orden

Estados actuales definidos:

| Estado | Descripción |
|---------|-------------|
| PENDING_PAYMENT | Orden creada esperando confirmación de pago. |
| PAID | Pago confirmado. |
| CANCELLED | Orden cancelada. |

Nuevos estados podrán incorporarse conforme evolucione el dominio.

---

# Seguridad

La API utilizará autenticación basada en JWT.

Roles previstos:

| Rol | Permisos |
|------|-----------|
| ROLE_USER | Crear y consultar sus órdenes. |
| ROLE_ADMIN | Consultar y administrar órdenes. |

---

# Respuestas

Las respuestas seguirán un formato consistente para operaciones exitosas y errores.

Los errores de negocio serán manejados mediante excepciones centralizadas utilizando los componentes compartidos de `common-lib`.

---

# Integración

Actualmente Order Service consume eventos provenientes de Product Service para mantener sincronizado `ProductCatalog`.

En futuras versiones también publicará eventos relacionados con el ciclo de vida de las órdenes.

Eventos futuros:

- ORDER_CREATED
- ORDER_PAID
- ORDER_CANCELLED

---

# Observaciones

La documentación funcional de la API se complementa con:

- domain.md
- order-flow.md
- event-consumption.md
- synchronization.md
- product-catalog.md

La documentación técnica de contratos HTTP estará disponible mediante OpenAPI/Swagger.