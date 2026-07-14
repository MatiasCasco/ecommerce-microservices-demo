# Product Catalog Synchronization

## Objetivo

Order Service mantiene una copia local del catálogo de productos para evitar llamadas síncronas hacia Product Service durante la creación de órdenes.

Esta copia local se denomina **ProductCatalog** y se sincroniza mediante eventos publicados por Product Service.

---

# Estrategia

Product Service continúa siendo la única fuente de verdad del catálogo.

Order Service nunca modifica directamente la información del catálogo.

Toda actualización proviene exclusivamente de eventos.

```text
           Product Service
                  │
                  ▼
         ProductUpdatedEvent
                  │
                  ▼
              RabbitMQ
                  │
                  ▼
          Order Service
                  │
                  ▼
         ProductCatalog
                  │
                  ▼
       Base de datos local
```

---

# Modelo de sincronización

El catálogo local funciona como una **proyección**.

No representa el catálogo oficial.

Representa una copia optimizada para lectura.

Su objetivo es permitir que Order Service pueda validar productos sin depender de otros microservicios.

---

# Eventos de sincronización

Cada evento modifica únicamente la información necesaria.

| Evento | Acción |
|----------|---------|
| PRODUCT_CREATED | Crear registro local |
| PRODUCT_UPDATED | Actualizar información |
| PRODUCT_ACTIVATED | Cambiar estado a ACTIVE |
| PRODUCT_DEACTIVATED | Cambiar estado a INACTIVE |
| PRODUCT_STOCK_UPDATED | Actualizar stock disponible |

---

# Consistencia

El sistema implementa **Consistencia Eventual**.

Esto significa que puede existir un pequeño retraso entre la actualización realizada en Product Service y su reflejo en ProductCatalog.

Durante condiciones normales ese retraso es mínimo y aceptable para el dominio.

---

# Beneficios

La sincronización mediante eventos permite:

- reducir el acoplamiento entre servicios
- eliminar llamadas REST
- mejorar la disponibilidad
- disminuir la latencia
- escalar servicios de forma independiente

---

# Fuente de verdad

Es importante recordar que:

ProductCatalog **no es** la fuente oficial del catálogo.

La fuente oficial continúa siendo Product Service.

ProductCatalog únicamente representa una proyección utilizada por Order Service.

---

# Evolución

En futuras iteraciones esta estrategia podrá complementarse con:

- mecanismos de reconciliación
- re-sincronización completa del catálogo
- detección de inconsistencias
- métricas de sincronización
- monitoreo del estado de la proyección