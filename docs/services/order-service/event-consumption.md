# Event Consumption

## Objetivo

Order Service consume eventos publicados por Product Service para mantener sincronizada una copia local del catálogo de productos.

Esta estrategia permite desacoplar ambos servicios y elimina la necesidad de realizar llamadas REST durante la creación de órdenes.

La información recibida se utiliza exclusivamente para mantener actualizada la proyección local (`ProductCatalog`).

---

# Flujo de Consumo

```text
                 Product Service
                        │
                        ▼
               ProductCreatedEvent
                        │
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

El mismo flujo aplica para todos los eventos relacionados con productos.

---

# Eventos Consumidos

Actualmente Order Service consume los siguientes eventos:

| Evento | Propósito |
|---------|-----------|
| PRODUCT_CREATED | Crear un nuevo producto en ProductCatalog |
| PRODUCT_UPDATED | Actualizar información del producto |
| PRODUCT_ACTIVATED | Marcar un producto como activo |
| PRODUCT_DEACTIVATED | Marcar un producto como inactivo |
| PRODUCT_STOCK_UPDATED | Sincronizar el stock disponible |

---

# Responsabilidades del Consumer

El Consumer únicamente recibe el evento y delega el procesamiento.

Su responsabilidad es:

- recibir el mensaje
- registrar logs
- propagar TraceId
- delegar la lógica al Service

Toda la lógica de negocio pertenece a `ProductCatalogService`.

---

# ProductCatalogService

Es responsable de aplicar los cambios sobre la proyección local.

Cada evento modifica únicamente la información necesaria.

Ejemplos:

- crear productos
- actualizar datos
- activar productos
- desactivar productos
- actualizar stock

---

# ProductCatalog

ProductCatalog representa una proyección local del catálogo.

No es la fuente oficial de información.

El dueño del catálogo continúa siendo Product Service.

Order Service nunca modifica ProductCatalog mediante operaciones propias del negocio.

Toda modificación proviene exclusivamente de eventos.

---

# Beneficios

Esta arquitectura ofrece:

- Bajo acoplamiento entre microservicios.
- Mayor disponibilidad.
- Eliminación de llamadas REST.
- Menor latencia durante la creación de órdenes.
- Consistencia eventual mediante eventos.

---

# Evolución

Actualmente los eventos únicamente sincronizan el catálogo.

En futuras iteraciones Order Service también publicará eventos propios.

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

De esta forma Order Service evolucionará de consumidor de eventos a productor y consumidor dentro de la arquitectura Event-Driven.