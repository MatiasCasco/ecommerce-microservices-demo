# Event Consumption

## Objetivo

Este documento describe cómo Order Service consume eventos publicados por Product Service para mantener actualizada su proyección local `ProductCatalog`.

El objetivo es documentar:

- qué eventos consume Order Service;
- el flujo de consumo;
- las responsabilidades del Consumer;
- la responsabilidad de `ProductCatalogService`;
- el papel de `ProductCatalog`.

La sincronización de la proyección se complementa con `product-catalog.md` y `synchronization.md`.

---

# Flujo de Consumo

```text
Product Service
      │
      │ Product Event
      ▼
   RabbitMQ
      │
      ▼
ProductCatalogConsumer
      │
      ▼
ProductCatalogService
      │
      ▼
ProductCatalog
```

El mismo flujo aplica para los eventos relacionados con productos que Order Service consume.

---

# Eventos Consumidos

Actualmente Order Service consume:

| Evento | Propósito |
|---|---|
| `PRODUCT_CREATED` | Crear un producto en `ProductCatalog` |
| `PRODUCT_UPDATED` | Actualizar la información del producto |
| `PRODUCT_ACTIVATED` | Marcar el producto como `ACTIVE` |
| `PRODUCT_DEACTIVATED` | Marcar el producto como `INACTIVE` |
| `PRODUCT_STOCK_UPDATED` | Actualizar el stock disponible |

---

# ProductCatalogConsumer

El Consumer es responsable de recibir los eventos y delegar su procesamiento.

Sus responsabilidades son:

- recibir el mensaje;
- registrar logs;
- propagar el `TraceId`;
- delegar el procesamiento a `ProductCatalogService`.

El Consumer no contiene la lógica de negocio de actualización de la proyección.

```text
ProductCatalogConsumer
        │
        └── delega
              ▼
      ProductCatalogService
```

---

# ProductCatalogService

`ProductCatalogService` aplica los cambios recibidos sobre la proyección local.

Cada tipo de evento modifica la información correspondiente:

```text
PRODUCT_CREATED
        │
        ▼
createProduct()


PRODUCT_UPDATED
        │
        ▼
updateProduct()


PRODUCT_ACTIVATED
        │
        ▼
activateProduct()


PRODUCT_DEACTIVATED
        │
        ▼
deactivateProduct()


PRODUCT_STOCK_UPDATED
        │
        ▼
updateStock()
```

La lógica de actualización de la proyección pertenece al Service y no al Consumer.

---

# ProductCatalog

`ProductCatalog` representa una proyección local del catálogo.

No es la fuente oficial de información.

```text
Product Service
      │
      └── Source of Truth

ProductCatalog
      │
      └── Proyección local de Order Service
```

El dueño del catálogo continúa siendo Product Service.

Los cambios del `ProductCatalog` provienen del procesamiento de eventos de Product Service.

---

# Desacoplamiento

El consumo de eventos permite que Order Service mantenga su información local sin realizar llamadas REST a Product Service durante la creación de una Order.

```text
Modelo síncrono

Order Service ───── REST ─────► Product Service


Modelo actual

Product Service
      │
      │ eventos
      ▼
   RabbitMQ
      │
      ▼
ProductCatalog
      │
      ▼
Order Service
```

Esto reduce el acoplamiento síncrono entre ambos servicios.

---

# Consistencia Eventual

La proyección local se mantiene mediante eventos.

Por lo tanto, existe consistencia eventual entre:

```text
Product Service
      │
      │ evento
      ▼
ProductCatalog
```

Durante el intervalo entre la modificación del producto y el procesamiento del evento, `ProductCatalog` puede representar temporalmente un estado anterior.

Esta característica es parte del modelo basado en eventos.

---

# Responsabilidades

La separación de responsabilidades queda definida así:

```text
Product Service
    │
    └── publica cambios del catálogo

RabbitMQ
    │
    └── transporta los eventos

ProductCatalogConsumer
    │
    └── recibe y delega

ProductCatalogService
    │
    └── aplica el cambio

ProductCatalog
    │
    └── mantiene la proyección local
```

---

# Lo que no hace el Consumer

`ProductCatalogConsumer` no es responsable de:

- gestionar el catálogo original;
- decidir las reglas del dominio de Order;
- construir Orders;
- calcular totales;
- reservar stock;
- realizar llamadas REST a Product Service.

Su responsabilidad termina en recibir el evento y delegar su procesamiento.

---

# Relación con Order

Los eventos consumidos no modifican directamente una Order existente.

Su propósito actual es mantener disponible la información operacional necesaria en `ProductCatalog`.

```text
Product Event
      │
      ▼
ProductCatalog
      │
      │ lectura durante Create Order
      ▼
OrderItem
      │
      │ snapshot
      ▼
Order
```

Una vez creado el `OrderItem`, la información histórica de la compra pertenece al dominio de Order y no vuelve a depender del evento original.

---

# Evolución

Actualmente Order Service actúa como consumidor de eventos de Product Service para mantener `ProductCatalog`.

Una evolución posterior permitirá que Order Service también publique eventos propios.

```text
Create Order
      │
      ▼
OrderCreatedEvent
      │
      ▼
RabbitMQ
      │
      ├── Notification Service
      ├── Payment Service
      ├── Inventory Service
      └── Analytics
```

Esta publicación de eventos de Order no forma parte del consumo actual documentado aquí.

La evolución de la mensajería se documenta en `roadmap.md` y `future.md`.
