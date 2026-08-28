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

# Arquitectura
 
 Order Service utiliza Hexagonal Architecture como estrategia de organizacion interna.

 La arquitectura no modifica las decisiones funcionales ni de dominio previamente definidas.

 La separacion sera:
 - Domain
 - Application
 - Ports
 - Adapters
 - Infrastructure

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

---

## Identidad de Order

### Problema

La Order necesita un identificador único que permita identificarla de forma estable dentro del sistema y utilizarlo como referencia para consultas y operaciones posteriores.

### Decisión

El identificador de `Order` será generado por la base de datos.

El identificador será incremental.

La generación del identificador pertenece a la infraestructura de persistencia y no al dominio de negocio.

### Justificación

- garantiza unicidad a nivel de persistencia;
- evita que el dominio tenga que conocer el mecanismo de generación;
- simplifica la persistencia de `Order`;
- permite utilizar el identificador como referencia estable de la Order.

### Consecuencias

El dominio no debe depender de una implementación específica del mecanismo de generación del ID.

La estrategia concreta de generación será responsabilidad de la capa de persistencia.

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

## Consolidación de Productos Duplicados

### Problema

El request de creación puede contener más de una línea correspondiente al mismo `productId`.

Por ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
```

Si estas líneas se transformaran directamente en `OrderItem`, se violaría la invariante de que un `productId` no puede repetirse dentro de una Order.

### Decisión

Antes de construir los `OrderItem`, el flujo de Application normalizará los productos solicitados consolidando las cantidades correspondientes al mismo `productId`.

Ejemplo:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
```

se transforma en:

```text
productId = 10, quantity = 5
```

La Order resultante contendrá como máximo un `OrderItem` por `productId`.

### Justificación

- mantiene la invariante del Aggregate;
- evita duplicidad de líneas para un mismo producto;
- simplifica el cálculo del total;
- facilita futuras representaciones de la Order, especialmente Invoice;
- normaliza el input antes de construir el Aggregate.

### Consecuencias

La consolidación pertenece al flujo de Application previo a la construcción de `OrderItem`.

El dominio continúa siendo responsable de proteger la invariante de que un `productId` no aparezca más de una vez.

Los duplicados no se consideran un error de negocio.

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

## Precio y Consistencia Eventual

### Problema

`ProductCatalog` es una proyección local y puede estar temporalmente desactualizado respecto de Product Service.

Esto puede afectar temporalmente el precio conocido por Order Service.

### Decisión

Order Service utilizará el precio conocido por `ProductCatalog` al momento de crear la Order.

Ese precio se copiará a `OrderItem.unitPrice` como snapshot histórico.

La consistencia eventual del precio se acepta explícitamente en el MVP.

### Justificación

- mantiene el desacoplamiento entre Order Service y Product Service;
- evita una llamada REST síncrona durante la creación;
- conserva el precio utilizado en la compra;
- permite que la Order sea históricamente reconstruible.

### Consecuencias

Puede existir temporalmente:

```text
Product Service
price = 150

ProductCatalog
price = 100
```

Si Order Service conoce `100`, ese será el precio utilizado para construir la Order.

Una vez creada:

```text
OrderItem.unitPrice = 100
```

queda congelado.

No se agrega una llamada síncrona a Product Service para garantizar que el precio sea el último publicado.

Si en el futuro el negocio requiere reglas de pricing más sofisticadas, podrá incorporarse una capability o servicio de `Pricing / Offer`.

No se implementa Pricing / Offer como parte del MVP.

---


# Decisiones del Customer y Ownership

## CustomerId como Identificador de Ownership

### Problema

Order Service necesita identificar a quién pertenece una Order, pero no necesita modelar toda la entidad Customer.

### Decisión

`Order` almacena únicamente:

```text
customerId
```

`customerId` es un identificador de ownership, no una entidad `Customer` dentro de Order Service.

No se implementará una Customer Projection en el MVP.

### Justificación

- mantiene desacoplado Order Service;
- evita duplicar el modelo de Customer;
- permite distinguir ownership de autenticación;
- deja abierta una futura Customer Projection si aparece una necesidad concreta.

### Consecuencias

Order Service no necesita conocer en el MVP:

- nombre;
- apellido;
- email;
- teléfono;
- dirección;
- otros datos completos de Customer.

No se realizará una llamada síncrona a Customer Service solamente para validar la existencia del Customer.

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

Las reglas detalladas se documentan en `security-authorization.md`.

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

# Decisiones de API y Consistencia

## DTO Único para Create Order

### Problema

USER y ADMIN tienen diferentes reglas para determinar `customerId`, pero no representan dos casos de uso de dominio distintos.

### Decisión

Se utilizará un único DTO HTTP:

```text
CreateOrderRequest
├── customerId?
└── items[]
    └── CreateOrderItemRequest
        ├── productId
        └── quantity
```

La diferencia entre USER y ADMIN se resuelve en Application / Authorization / Ownership.

### Justificación

- evita duplicar contratos HTTP;
- mantiene un único caso de uso `Create Order`;
- separa autorización de dominio;
- permite que el mismo contrato evolucione sin duplicación.

### Consecuencias

Para USER:

```text
customerId = customerId del actor autenticado
```

Para ADMIN:

```text
customerId = customerId indicado en el request
```

cuando la autorización lo permita.

---

## Order Response y Order Summary

### Decisión

La API utilizará representaciones diferentes para detalle y listado.

Para detalle:

```text
GET /orders/{id}
    ↓
OrderResponse
```

`OrderResponse` incluye los `OrderItem` completos.

Para listado:

```text
GET /orders
    ↓
Page<OrderSummaryResponse>
```

`OrderSummaryResponse` no incluye `items`.

### Justificación

- evita cargar todas las líneas de cada Order en listados;
- separa la representación de detalle de la representación resumida;
- facilita paginación;
- reduce payload innecesario.

---

## Idempotencia de Create Order

### Problema

Una solicitud `POST /orders` puede ser reenviada por el cliente debido a timeout, retry, pérdida de la respuesta HTTP o problemas de red.

Sin idempotencia, el mismo intento lógico podría generar múltiples Orders.

### Decisión

`POST /orders` requiere obligatoriamente el header:

```text
Idempotency-Key
```

Ejemplo:

```http
POST /orders
Idempotency-Key: 7f8c9a...
```

La clave identifica un intento lógico de creación de Order.

### Justificación

- evita Orders duplicadas ante reintentos;
- hace seguro el retry del cliente;
- protege una operación con efecto persistente;
- permite que el cliente reintente una solicitud sin generar una nueva Order.

### Consecuencias

La idempotencia de `POST /orders` forma parte del MVP.

Conceptualmente se mantendrá una relación:

```text
Idempotency-Key
        ↓
resultado de la operación
        ↓
Order
```

La información necesaria para garantizar la idempotencia deberá persistirse en PostgreSQL.

La creación de la Order, sus `OrderItem` y el registro de idempotencia deberán mantener consistencia transaccional.

---

## Misma Idempotency-Key con el mismo Request

### Decisión

Si una solicitud se repite utilizando:

```text
misma Idempotency-Key
+
mismo request
```

se considerará el mismo intento lógico de creación.

El sistema devolverá el resultado de la operación original y no creará una nueva Order.

### Justificación

Permite reintentar de forma segura una operación cuya respuesta original pudo perderse.

---

## Misma Idempotency-Key con un Request Diferente

### Decisión

Si una `Idempotency-Key` previamente utilizada llega asociada a un request diferente, la operación será rechazada.

Conceptualmente:

```text
Idempotency-Key
      +
Request fingerprint
```

permite distinguir:

```text
misma key + mismo request
    → resultado original

misma key + request diferente
    → error
```

### Justificación

Evita reutilizar accidentalmente una clave para representar operaciones diferentes.

---

## Persistencia de Idempotencia

### Decisión

La información de idempotencia se persistirá en la misma base de datos de Order Service.

Conceptualmente:

```text
PostgreSQL
├── orders
├── order_items
└── idempotency_records
```

La creación de Order y el registro de idempotencia deben formar parte de una operación transaccional consistente.

### Justificación

No se introduce Redis, Caffeine u otro almacenamiento externo solamente para resolver idempotencia.

La garantía debe sobrevivir a reinicios del servicio.

### Consecuencias

Conceptualmente:

```text
BEGIN

    crear Order
    crear OrderItems
    guardar IdempotencyRecord

COMMIT
```

Si la transacción falla:

```text
ROLLBACK
```

No debe quedar una Order persistida sin su registro de idempotencia ni viceversa.

---

# Decisiones sobre Errores de Negocio

## Separación entre Errores de Negocio e Infraestructura

### Decisión

Los errores que representan reglas del negocio deben mantenerse separados de fallos de infraestructura.

Casos de negocio relevantes:

```text
ProductNotFound
ProductInactive
InsufficientStock
OrderNotFound
OrderNotCancellable
UnauthorizedOrderAccess
InvalidOrderItem
```

Los duplicados de productos no son un error: se consolidan.

### Justificación

Permite que Application y API distingan correctamente entre:

```text
regla de negocio incumplida
```

y:

```text
fallo técnico
```

Los fallos de base de datos, RabbitMQ u otros componentes de infraestructura no deben convertirse artificialmente en errores de negocio.

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

## Pricing / Offer como Evolución Futura

### Problema

El MVP acepta que `ProductCatalog` pueda tener un precio temporalmente desactualizado debido a la consistencia eventual.

En el futuro pueden aparecer necesidades de pricing más sofisticadas.

### Decisión

No implementar `Pricing / Offer` en el MVP.

Si el negocio requiere posteriormente:

- promociones;
- descuentos;
- cupones;
- precios por Customer;
- precios temporales;
- reglas comerciales de pricing;
- garantías de precio más sofisticadas;

se podrá incorporar una capability o servicio dedicado de `Pricing / Offer`.

### Justificación

Evita introducir un servicio prematuramente y mantiene el desacoplamiento actual.

---

## Patrones de Resiliencia y Mensajería como Evolución

### Decisión

Los siguientes patrones se consideran evolución futura y no forman parte de las decisiones necesarias para implementar el MVP actual:

- Retry;
- DLQ;
- Publisher Confirms;
- consumidores idempotentes;
- Outbox Pattern;
- Saga.

### Justificación

Estos mecanismos resuelven problemas distintos a la idempotencia HTTP de `POST /orders`.

La idempotencia HTTP:

```text
cliente
    ↓
POST /orders
    ↓
retry
```

evita crear Orders duplicadas.

Los patrones de mensajería resuelven problemas relacionados con:

```text
eventos
mensajería
entrega
fallos de consumidores
publicación confiable
coordinación distribuida
```

### Consecuencias

No se implementan anticipadamente.

Se incorporarán cuando el flujo real del sistema requiera resolver esos problemas.

---

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
- la creación de Order es idempotente;
- la reserva de stock pertenece a Inventory;
- el sistema debe evolucionar incrementalmente;
- se evita sobreingeniería.

La regla general es:

```text
Diseñar para evolucionar
          ↓
Implementar solamente
lo que necesita el negocio actual
```
