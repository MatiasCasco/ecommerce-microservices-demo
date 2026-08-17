# Order Service - Design Decisions

## Objetivo

Este documento registra las principales decisiones de diseño adoptadas durante el diseño de Order Service.

Su propósito es conservar:

- el problema que motivó una decisión;
- la decisión adoptada;
- su justificación;
- las consecuencias relevantes.

Las decisiones específicas del dominio de Order Service se documentan aquí.

Las decisiones transversales de toda la arquitectura se documentan mediante ADR cuando corresponda.

---

# Decisiones del Dominio

## Order como Aggregate Root

### Problema

Una Order está compuesta por múltiples `OrderItem` y era necesario definir quién protege la consistencia del conjunto.

### Decisión

`Order` será el **Aggregate Root**.

Toda modificación del Aggregate deberá realizarse a través de `Order`.

### Justificación

- protege la consistencia del Aggregate;
- centraliza las invariantes;
- evita modificaciones independientes de `OrderItem`;
- mantiene las reglas del negocio dentro del dominio.

---

## OrderItem como Snapshot Histórico

### Problema

Los productos pueden cambiar de nombre, precio, stock o estado después de una compra.

### Decisión

Cada `OrderItem` conservará la información necesaria para reconstruir lo que fue comprado.

Incluye:

- `productId`;
- `productName`;
- `unitPrice`;
- `quantity`;
- `subtotal`.

### Justificación

La Order representa la compra en el momento en que fue realizada.

Los cambios posteriores del catálogo no deben modificar el historial de una Order existente.

---

## Inmutabilidad del Contenido Comercial

### Problema

Una Order representa un registro histórico. Permitir modificaciones posteriores podría alterar el significado de la compra original.

### Decisión

Una vez creada una Order, su contenido comercial no cambia.

No se modifican:

- `productId`;
- `productName`;
- `unitPrice`;
- `quantity`;
- `subtotal`;
- `total`;
- `customerId`.

### Justificación

La Order debe poder reconstruirse históricamente sin depender de información actualizada del catálogo.

---

## ProductCatalog como Proyección Local

### Problema

Consultar Product Service mediante REST durante cada creación de Order incrementa el acoplamiento y genera una dependencia síncrona entre servicios.

### Decisión

Order Service mantiene una proyección local denominada `ProductCatalog`.

Esta proyección se sincroniza mediante eventos publicados por Product Service.

### Justificación

- reduce el acoplamiento síncrono;
- evita depender de Product Service durante la creación de una Order;
- permite trabajar con información local;
- mantiene a Product Service como Source of Truth.

---

# Decisiones del Modelo

## El Cliente No Define Precios

### Problema

Permitir que el cliente envíe precios comprometería la integridad de la información económica.

### Decisión

El cliente únicamente proporciona la información necesaria para solicitar la compra:

- productos;
- cantidades;
- `customerId` cuando corresponda según el actor.

El cliente no define:

- `unitPrice`;
- `subtotal`;
- `total`.

### Justificación

Los importes deben ser determinados por el servidor utilizando la información de `ProductCatalog`.

Las reglas específicas de `USER`, `ADMIN` y ownership se documentan en `security-authorization.md`.

---

## El Dominio Calcula los Importes

### Problema

Era necesario definir dónde debía residir la lógica de cálculo.

### Decisión

`OrderItem` calcula su subtotal.

`Order` calcula el total.

### Justificación

El cálculo forma parte de las reglas del dominio.

La capa Application coordina el caso de uso, pero no se convierte en dueña de estas reglas.

---

# Decisiones del Ciclo de Vida

## Estado Inicial

### Decisión

Toda nueva Order comienza en:

```text
PENDING_PAYMENT
```

### Justificación

Una Order creada no implica que el pago haya sido confirmado.

El pago representa un proceso independiente.

---

## Transiciones de Estado

### Decisión

En el MVP las transiciones permitidas son:

```text
PENDING_PAYMENT
       │
       ├──► PAID
       │
       └──► CANCELLED
```

`PAID` y `CANCELLED` son estados terminales.

### Justificación

El Aggregate debe controlar las transiciones válidas y evitar cambios arbitrarios de estado.

El pago no se representa como un simple cambio administrativo del estado de la Order.

---

## Las Órdenes No Se Eliminan

### Problema

Eliminar una Order implicaría perder información histórica.

### Decisión

Las Orders nunca se eliminan físicamente como parte del lifecycle del dominio.

Su estado representa su evolución.

### Justificación

Permite conservar:

- auditoría;
- trazabilidad;
- métricas;
- análisis del negocio.

---

# Decisiones de Integración

## Product Service como Source of Truth

### Decisión

Product Service continúa siendo el dueño del catálogo.

Order Service no modifica el catálogo original.

### Justificación

Evita tener múltiples fuentes de verdad para la misma información.

---

## ProductCatalog Solo como Proyección

### Decisión

`ProductCatalog` se utiliza para lectura por parte de Order Service.

Las operaciones de negocio de Order no modifican directamente la proyección como si fuera el catálogo original.

### Justificación

Se mantiene la separación:

```text
Product Service
      │
      └── Source of Truth

ProductCatalog
      │
      └── Proyección local
```

---

## Sincronización Mediante Eventos

### Decisión

ProductCatalog se sincroniza mediante eventos publicados por Product Service.

### Justificación

El modelo evita una dependencia REST síncrona durante la creación de Orders y permite que Order Service mantenga una representación local del catálogo.

Los detalles de esta sincronización se documentan en:

- `synchronization.md`;
- `event-consumption.md`;
- `product-catalog.md`.

---

# Decisiones sobre Stock

## Stock Disponible No Es Reserva

### Problema

Consultar `availableStock` desde una proyección local no garantiza por sí mismo que una unidad quede reservada para una Order.

Existe una posible condición de carrera:

```text
Stock = 1

Order A ──► observa 1
Order B ──► observa 1
```

Ambas podrían intentar comprar la misma unidad.

### Decisión

La validación de stock pertenece al flujo actual, pero la reserva de inventario no será implementada dentro de Order Service como una solución improvisada.

La arquitectura evolucionará hacia una responsabilidad dedicada de Inventory / Reservation.

### Justificación

Se evita mezclar la responsabilidad de inventario con el Aggregate Order y se deja abierta una evolución hacia una solución consistente de reserva.

---

# Decisiones de Arquitectura del Dominio

## Separación entre Dominio y Seguridad

### Decisión

`Order` no conoce:

- JWT;
- Spring Security;
- `SecurityContext`;
- roles;
- mecanismos de autenticación.

### Justificación

Authentication, Authorization, Ownership y Business Rules son responsabilidades diferentes.

La representación del actor autenticado y las reglas de autorización se documentan en:

```text
security-authorization.md
```

---

## Actor no es Customer

### Decisión

El actor que ejecuta una operación no se modela como el Customer propietario de la Order.

```text
Actor ≠ Customer
```

El `customerId` pertenece al estado de la Order.

La forma en que se determina ese identificador depende del actor y del caso de uso.

### Justificación

Permite distinguir:

- quién ejecuta la operación;
- para quién se realiza la operación;
- qué reglas de ownership aplican.

---

# Decisiones sobre Evolución

## No Implementar Customer Projection Prematuramente

### Problema

Order Service puede necesitar validar o consultar información de Customer en el futuro.

Crear una proyección desde el comienzo introduciría infraestructura y consistencia eventual sin una necesidad actual suficientemente fuerte.

### Decisión

No implementar Customer Projection en el MVP.

Actualmente Order necesita principalmente la referencia:

```text
customerId
```

### Justificación

Se evita sobreingeniería.

La arquitectura puede evolucionar posteriormente hacia una Customer Projection si aparece una necesidad concreta.

---

## Order Creation no Equivale a Stock Reservation

### Decisión

Crear una Order no implica que Inventory haya reservado stock.

```text
Order CREATED
      ≠
Stock RESERVED
```

### Justificación

La reserva requiere una responsabilidad específica de inventario y coordinación que todavía no forma parte del MVP.

---

# Principio General

Las decisiones de Order Service deben respetar:

- el dominio guía la implementación;
- `Order` protege sus invariantes;
- `OrderItem` conserva el snapshot histórico;
- Product Service es el dueño del catálogo;
- `ProductCatalog` es una proyección local;
- las reglas de negocio pertenecen al dominio;
- los eventos reducen el acoplamiento entre servicios;
- las responsabilidades deben permanecer separadas;
- el sistema debe evolucionar incrementalmente;
- se evita sobreingeniería.

La regla general es:

```text
Diseñar para evolucionar
          ↓
Implementar solamente
lo que necesita el negocio actual
```
