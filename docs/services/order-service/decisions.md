# Order Service - Design Decisions

## Objetivo

Este documento registra las principales decisiones de diseño adoptadas durante el desarrollo de **Order Service**.

Su propósito es explicar el razonamiento detrás del modelo de dominio, facilitando el mantenimiento, la evolución del sistema y la incorporación de nuevos desarrolladores.

Las decisiones aquí documentadas son específicas de Order Service. Las decisiones que afectan a toda la arquitectura del sistema se documentan como Architecture Decision Records (ADR).

---

# Decisiones del Dominio

## Order como Aggregate Root

### Problema

Una orden está compuesta por múltiples productos (OrderItems). Era necesario definir quién sería responsable de mantener la consistencia del agregado.

### Decisión

`Order` será el **Aggregate Root**.

Toda modificación del agregado deberá realizarse exclusivamente a través de Order.

### Justificación

- Mantiene la consistencia del agregado.
- Centraliza las reglas de negocio.
- Evita modificaciones parciales de los OrderItems.
- Sigue los principios de Domain-Driven Design (DDD).

---

## OrderItem representa un Snapshot

### Problema

Los productos pueden cambiar de nombre, precio o incluso ser eliminados del catálogo después de una compra.

### Decisión

Cada OrderItem almacenará la información necesaria para reconstruir la compra.

Incluye:

- productId
- productName
- unitPrice
- quantity
- subtotal

### Justificación

La orden representa una fotografía del momento en que se realizó la compra.

Los cambios posteriores en Product Service no deben modificar el historial de una orden.

---

## ProductCatalog como Proyección Local

### Problema

Consultar Product Service mediante REST durante cada compra incrementa el acoplamiento, la latencia y la dependencia entre microservicios.

### Decisión

Order Service mantendrá una copia local del catálogo (`ProductCatalog`) sincronizada mediante eventos.

### Justificación

- Reduce el acoplamiento.
- Disminuye la latencia.
- Mejora la disponibilidad.
- Favorece una arquitectura Event-Driven.

---

# Decisiones del Modelo

## El cliente nunca envía precios

### Problema

Permitir que el cliente envíe el precio de un producto compromete la integridad de la información.

### Decisión

El Request únicamente enviará:

- customerId
- productId
- quantity

Toda la información económica será obtenida desde ProductCatalog.

### Justificación

El servidor es el único responsable de calcular importes.

---

## El dominio calcula los importes

### Problema

Era necesario definir dónde reside la lógica de cálculo.

### Decisión

Cada OrderItem calculará su subtotal.

Order calculará el total de la compra.

### Justificación

La lógica pertenece al dominio y no a la capa de servicios.

Los Services únicamente coordinan el caso de uso.

---

## Las órdenes son documentos históricos

### Problema

Una orden debe conservar información histórica incluso cuando el catálogo evoluciona.

### Decisión

Una vez creada una orden:

- no cambia el nombre del producto
- no cambia el precio
- no cambia la cantidad
- no cambia el subtotal

### Justificación

La orden representa un documento contable y de auditoría.

---

# Decisiones del Ciclo de Vida

## Estado inicial

### Decisión

Toda nueva orden comenzará con el estado:

```text
PENDING_PAYMENT
```

### Justificación

Una orden no nace pagada.

El pago representa un proceso independiente que ocurrirá posteriormente.

---

## Las órdenes no se eliminan

### Problema

Eliminar órdenes implica perder información valiosa para auditoría y análisis del negocio.

### Decisión

Las órdenes cambiarán de estado.

Nunca serán eliminadas.

### Justificación

Permite:

- auditoría
- trazabilidad
- métricas
- análisis comercial

---

# Decisiones de Integración

## Sincronización mediante eventos

### Decisión

ProductCatalog se mantendrá sincronizado exclusivamente mediante eventos publicados por Product Service.

### Justificación

Order Service no es dueño del catálogo.

Product Service continúa siendo la única fuente de verdad.

---

## Separación entre lectura y escritura

### Decisión

ProductCatalog será utilizado únicamente para lectura.

Las operaciones de negocio nunca modificarán directamente esta proyección.

### Justificación

Se evita romper el principio de "Single Source of Truth".

---

# Decisiones Futuras

## Reserva de Stock

Durante el análisis del dominio se evaluaron dos estrategias.

### Opción A

Reservar stock al crear una orden.

Ventajas:

- evita overselling
- mejora la experiencia del cliente
- comportamiento similar al de grandes plataformas de e-commerce

### Opción B

Descontar stock únicamente después del pago.

Ventajas:

- implementación más simple

Desventajas:

- posibilidad de vender más unidades de las disponibles

### Decisión

La arquitectura evolucionará hacia la **reserva de stock**, pero esta funcionalidad será implementada en una etapa posterior cuando exista un servicio dedicado al inventario.

---

# Principio General

Todas las decisiones adoptadas en Order Service deberán respetar los siguientes principios:

- El dominio guía la implementación.
- Product Service es el dueño del catálogo.
- Order representa un documento histórico.
- Las reglas de negocio pertenecen al dominio.
- Los eventos desacoplan los microservicios.
- El sistema debe evolucionar de forma incremental evitando sobreingeniería.