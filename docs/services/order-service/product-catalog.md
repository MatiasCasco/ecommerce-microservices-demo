# Product Catalog

## Objetivo

`ProductCatalog` es una proyección local del catálogo de productos mantenida por Order Service.

Su propósito es proporcionar la información necesaria para que Order Service pueda validar y construir una Order sin depender de consultas REST síncronas al Product Service.

`Product Service` continúa siendo el propietario del catálogo y su **Source of Truth**.

---

# Responsabilidad

`ProductCatalog` mantiene únicamente la información que Order Service necesita para trabajar con productos durante la creación de una Order.

Permite conocer:

- si el producto existe;
- el precio vigente conocido;
- el stock disponible conocido;
- el estado del producto.

```text
Product Service
      │
      │ estado actual
      ▼
ProductCatalog
      │
      │ información necesaria
      ▼
Create Order
```

---

# Source of Truth

Product Service es el dueño del catálogo.

```text
Product Service
      │
      └── Source of Truth
```

Order Service no modifica el catálogo original.

```text
Product Service
      │
      │ eventos
      ▼
ProductCatalog
      │
      └── proyección local
```

Por lo tanto:

```text
ProductCatalog ≠ Product Service
```

`ProductCatalog` representa una copia local especializada para las necesidades de Order Service.

---

# Información almacenada

Actualmente `ProductCatalog` mantiene:

- `id`
- `name`
- `price`
- `availableStock`
- `status`
- `categoryId`
- `updatedAt`

No se replica toda la información existente en Product Service.

La proyección contiene solamente los datos necesarios para las responsabilidades actuales de Order Service.

---

# Uso durante la creación de una Order

Cuando se crea una Order, Order Service utiliza `ProductCatalog` para obtener la información vigente conocida del producto.

```text
Create Order
      │
      ▼
ProductCatalog
      │
      ├── product exists?
      ├── status = ACTIVE?
      ├── availableStock?
      └── price
      │
      ▼
OrderItem
      │
      ├── productId
      ├── productName
      ├── unitPrice
      ├── quantity
      └── subtotal
```

El `price` utilizado para construir el `OrderItem` proviene de `ProductCatalog`.

Después de crear la Order, ese valor pasa a formar parte del snapshot histórico del `OrderItem`.

---

# ProductCatalog vs OrderItem

Son conceptos diferentes.

```text
ProductCatalog
      │
      │ estado actual conocido
      ▼
OrderItem
      │
      │ snapshot
      ▼
Order
```

### ProductCatalog

Representa información operacional y puede cambiar cuando Product Service publica nuevos eventos.

### OrderItem

Representa información histórica y no cambia después de creada la Order.

Por ejemplo:

```text
ProductCatalog
price = 5.300.000
```

puede cambiar posteriormente.

Pero:

```text
OrderItem
unitPrice = 4.500.000
```

permanece con el precio registrado al momento de la compra.

---

# Estado del producto

`ProductCatalog` mantiene el estado necesario para determinar si un producto puede utilizarse durante la creación de una Order.

Estados actuales:

```text
ACTIVE
INACTIVE
```

Conceptualmente:

```text
ProductCatalog
      │
      ├── ACTIVE
      │      └── puede utilizarse
      │
      └── INACTIVE
             └── no puede utilizarse
```

---

# Stock

`availableStock` representa el stock conocido por Order Service en su proyección local.

Puede utilizarse para validar:

```text
requestedQuantity <= availableStock
```

Sin embargo:

```text
stock disponible
      ≠
stock reservado
```

`ProductCatalog` no realiza reservas de inventario.

La reserva y el problema de concurrencia/overselling pertenecen a una futura responsabilidad de Inventory / Reservation.

---

# Consistencia

`ProductCatalog` utiliza **consistencia eventual**.

Existe un intervalo posible entre:

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

Por lo tanto, `ProductCatalog` puede representar temporalmente un estado anterior al estado actual de Product Service.

Esto es una consecuencia aceptada del desacoplamiento mediante eventos.

---

# Actualización de la Proyección

La proyección se actualiza mediante eventos publicados por Product Service.

```text
Product Service
      │
      │ Product Events
      ▼
RabbitMQ
      │
      ▼
ProductCatalog
```

Los eventos que alimentan actualmente la proyección son:

| Evento | Cambio en ProductCatalog |
|---|---|
| `ProductCreatedEvent` | Crear producto |
| `ProductUpdatedEvent` | Actualizar información |
| `ProductActivatedEvent` | Cambiar estado a `ACTIVE` |
| `ProductDeactivatedEvent` | Cambiar estado a `INACTIVE` |
| `ProductStockUpdatedEvent` | Actualizar stock |

El detalle del consumo y procesamiento de estos eventos se documenta en:

- `event-consumption.md`
- `synchronization.md`

---

# Principios

## Proyección local

`ProductCatalog` existe para que Order Service pueda trabajar con información de productos localmente.

No pretende convertirse en un segundo Product Service.

---

## Bajo acoplamiento

Order Service no necesita realizar una llamada REST a Product Service durante la creación de una Order.

```text
Antes:

Order Service ──REST──► Product Service


Modelo actual:

Product Service
      │
      │ events
      ▼
ProductCatalog
      │
      ▼
Order Service
```

---

## Información mínima necesaria

La proyección no replica información innecesaria.

Mantener solamente los datos necesarios permite:

- reducir acoplamiento;
- simplificar el modelo;
- reducir almacenamiento;
- limitar la dependencia entre dominios.

---

# Límites de ProductCatalog

`ProductCatalog` no es responsable de:

- gestionar productos;
- modificar productos en Product Service;
- representar el historial de una Order;
- calcular el total de una Order;
- gestionar pagos;
- reservar inventario;
- autenticar usuarios;
- autorizar operaciones.

Sus responsabilidades se limitan a representar una proyección local del catálogo necesaria para Order Service.

---

# Evolución

La proyección puede evolucionar si Order Service necesita información adicional del catálogo.

Cualquier nuevo atributo debe responder primero a una necesidad concreta del dominio o de un caso de uso.

No se debe replicar información de Product Service simplemente porque esté disponible.

La evolución de la sincronización, resiliencia y consumo de eventos se documenta en:

- `synchronization.md`
- `event-consumption.md`
- `roadmap.md`
- `future.md`
