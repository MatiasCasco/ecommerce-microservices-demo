# Product Service - Design Decisions

## Objetivo

Este documento registra las principales decisiones de diseño adoptadas durante el desarrollo de **Product Service**.

Su propósito es explicar el razonamiento detrás del modelo de dominio, facilitando el mantenimiento, la evolución del sistema y la incorporación de nuevos desarrolladores.

Las decisiones aquí documentadas son específicas de Product Service. Las decisiones que afectan a toda la arquitectura del ecosistema se documentan mediante Architecture Decision Records (ADR).

---

# Decisiones del Dominio

## Product Service como Single Source of Truth

### Problema

Era necesario definir qué microservicio sería responsable del catálogo de productos.

### Decisión

Product Service será la única fuente oficial de información relacionada con productos.

Todos los cambios del catálogo deberán realizarse exclusivamente desde este servicio.

### Justificación

- Evita inconsistencias.
- Centraliza las reglas del negocio.
- Simplifica la arquitectura.
- Permite sincronizar otros servicios mediante eventos.

---

## Modelo Operacional

### Problema

Los demás servicios necesitan conocer el estado actual de un producto.

### Decisión

Product Service representa el **modelo operacional** del sistema.

Siempre refleja la información vigente.

### Justificación

El catálogo debe mostrar:

- precio actual
- stock actual
- estado actual
- categoría actual

La información histórica pertenece a otros dominios, como Order Service.

---

# Decisiones del Modelo

## ACTIVE / INACTIVE

### Problema

Eliminar productos físicamente provoca pérdida de referencias históricas.

### Decisión

Los productos cambian de estado.

Nunca se eliminan físicamente.

Estados actuales:

- ACTIVE
- INACTIVE

### Justificación

Permite:

- conservar historial
- mantener integridad referencial
- reactivar productos
- simplificar auditorías

---

## Actualización de Stock Independiente

### Problema

Inicialmente el endpoint de actualización general permitía modificar también el stock.

Durante las pruebas se detectó que esto podía generar inconsistencias entre Product Service y las proyecciones mantenidas por Order Service.

### Decisión

El stock tendrá un caso de uso independiente.

```
PATCH /products/{id}/stock
```

La actualización general del producto no modificará el inventario.

### Justificación

El stock representa una responsabilidad diferente.

Separar ambas operaciones:

- reduce errores
- simplifica la sincronización
- evita modificaciones accidentales
- facilita futuras integraciones con Inventory Service

---

## Eventos Especializados

### Problema

Se evaluó publicar un único evento ProductUpdated para cualquier modificación.

### Decisión

Cada cambio relevante del dominio genera su propio evento.

Eventos implementados:

- PRODUCT_CREATED
- PRODUCT_UPDATED
- PRODUCT_ACTIVATED
- PRODUCT_DEACTIVATED
- PRODUCT_STOCK_UPDATED

### Justificación

Los consumidores reciben únicamente la información necesaria.

Cada evento representa un hecho de negocio específico.

---

# Decisiones de Integración

## Comunicación mediante Eventos

### Problema

Las llamadas REST incrementan el acoplamiento entre microservicios.

### Decisión

Los cambios del catálogo serán comunicados mediante eventos publicados en RabbitMQ.

### Justificación

Permite:

- desacoplar servicios
- mejorar disponibilidad
- facilitar escalabilidad
- incorporar nuevos consumidores sin modificar Product Service

---

## Product Service Desconoce a los Consumidores

### Problema

El productor no debe depender de quién consume los eventos.

### Decisión

Product Service únicamente publica eventos.

Nunca conoce qué microservicios los reciben.

### Justificación

Mantiene una arquitectura desacoplada.

Permite agregar nuevos consumidores sin modificar el productor.

---

# Decisiones de Consistencia

## Publicación Después de Persistir

### Problema

Era necesario garantizar que únicamente se publiquen eventos correspondientes a operaciones exitosas.

### Decisión

Los eventos se generan después de persistir exitosamente los cambios.

### Justificación

Evita publicar eventos sobre operaciones fallidas.

En futuras versiones esta estrategia evolucionará mediante Outbox Pattern.

---

## Consistencia Eventual

### Problema

Las proyecciones locales no se actualizan en la misma transacción que Product Service.

### Decisión

El sistema adopta un modelo de consistencia eventual.

### Justificación

Los consumidores sincronizan su información mediante eventos.

Un pequeño retraso entre productor y consumidores es aceptable dentro del dominio.

---

# Decisiones de Observabilidad

## Logging Estructurado

### Decisión

Toda operación relevante genera logs estructurados utilizando CommerceLog.

### Justificación

Facilita:

- auditoría
- monitoreo
- depuración
- trazabilidad

---

## Propagación de TraceId

### Decisión

Cada evento publicado incluye el TraceId de la operación que lo originó.

### Justificación

Permite seguir una operación completa entre múltiples microservicios.

---

# Decisiones Futuras

## Inventory Service

Actualmente Product Service administra el stock.

A futuro esta responsabilidad podrá migrar hacia un servicio especializado.

Product Service conservará únicamente la información del catálogo.

Inventory Service administrará:

- stock
- reservas
- movimientos
- disponibilidad

---

## Publicación Confiable

La publicación de eventos evolucionará incorporando:

- Outbox Pattern
- Publisher Confirms
- Retry
- Dead Letter Queue
- Idempotencia

El objetivo es garantizar que ningún evento del dominio se pierda.

---

# Principios Generales

Todas las futuras decisiones del servicio deberán respetar los siguientes principios:

- Product Service es la única fuente oficial del catálogo.
- El dominio es dueño de las reglas de negocio.
- Los eventos representan hechos del negocio.
- El productor permanece desacoplado de sus consumidores.
- La arquitectura evoluciona de manera incremental.
- La simplicidad prevalece sobre la sobreingeniería.