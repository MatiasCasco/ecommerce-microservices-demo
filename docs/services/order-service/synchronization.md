# Product Catalog Synchronization

## Objetivo

Order Service mantiene una proyección local del catálogo para evitar llamadas síncronas a Product Service durante la creación de Orders.

Esta proyección se denomina `ProductCatalog` y se sincroniza mediante eventos publicados por Product Service.

---

# Estrategia

Product Service continúa siendo la única fuente de verdad del catálogo.

Order Service no modifica directamente el catálogo original.

```text
Product Service
   Source of Truth
        │
        │ Product Events
        ▼
     RabbitMQ
        │
        ▼
   ProductCatalog
        │
        ▼
   Local Database
```

`ProductCatalog` es una proyección local optimizada para las necesidades de lectura de Order Service.

---

# Modelo de Sincronización

La sincronización es **event-driven**.

Cuando Product Service modifica el catálogo, publica un evento.

Order Service procesa ese evento y actualiza la información correspondiente en `ProductCatalog`.

```text
Cambio en Product Service
          │
          ▼
      Product Event
          │
          ▼
        RabbitMQ
          │
          ▼
      Order Service
          │
          ▼
      ProductCatalog
```

El detalle de cómo se reciben y procesan los eventos se documenta en `event-consumption.md`.

---

# Eventos de Sincronización

Actualmente la proyección se mantiene mediante:

| Evento | Actualización |
|---|---|
| `PRODUCT_CREATED` | Crear registro local |
| `PRODUCT_UPDATED` | Actualizar información |
| `PRODUCT_ACTIVATED` | Cambiar estado a `ACTIVE` |
| `PRODUCT_DEACTIVATED` | Cambiar estado a `INACTIVE` |
| `PRODUCT_STOCK_UPDATED` | Actualizar stock disponible |

Cada evento actualiza únicamente la información correspondiente de la proyección.

---

# Consistencia Eventual

La sincronización utiliza **consistencia eventual**.

Existe un intervalo entre:

```text
Product Service
      │
      │ cambio
      ▼
Product Event
      │
      ▼
RabbitMQ
      │
      ▼
ProductCatalog
```

Durante ese intervalo, `ProductCatalog` puede representar temporalmente un estado anterior al estado actual de Product Service.

Esta diferencia es una consecuencia aceptada del modelo de desacoplamiento mediante eventos.

---

# Implicación para Order Service

La creación de una Order utiliza el estado conocido en `ProductCatalog`.

Por lo tanto:

```text
Product Service
      │
      │ estado actual
      ▼
Product Event
      │
      ▼
ProductCatalog
      │
      │ estado conocido por Order Service
      ▼
Create Order
```

`ProductCatalog` permite evitar una dependencia REST síncrona durante la creación de la Order.

---

# Fuente de Verdad

Es importante mantener la separación:

```text
Product Service
      │
      └── Source of Truth


ProductCatalog
      │
      └── Proyección local
```

`ProductCatalog` no se convierte en un segundo catálogo oficial.

Si existe una diferencia temporal entre ambos modelos, Product Service continúa siendo la referencia oficial.

---

# Beneficios

La estrategia de sincronización permite:

- reducir el acoplamiento entre servicios;
- eliminar llamadas REST durante la creación de Orders;
- mejorar la disponibilidad del flujo de creación;
- disminuir la latencia;
- permitir que los servicios evolucionen de forma independiente.

---

# Evolución de la Sincronización

La estrategia actual puede evolucionar con mecanismos para detectar y corregir diferencias entre la fuente de verdad y la proyección.

Posibles evoluciones:

- reconciliación;
- re-sincronización completa del catálogo;
- detección de inconsistencias;
- métricas de sincronización;
- monitoreo del estado de la proyección.

Estas capacidades no forman parte del mecanismo actual.

---

# Separación de Responsabilidades

Cada documento mantiene una responsabilidad específica:

```text
product-catalog.md
    │
    └── qué representa ProductCatalog


event-consumption.md
    │
    └── cómo se consumen y procesan los eventos


synchronization.md
    │
    └── cómo y por qué se mantiene sincronizada la proyección
```

La sincronización se centra en la **estrategia y consistencia de la proyección**, no en la implementación del Consumer.
