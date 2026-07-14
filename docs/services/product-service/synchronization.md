# Synchronization

## Objetivo

Product Service mantiene sincronizados los demás microservicios mediante la publicación de eventos de dominio.

Cada modificación realizada sobre el catálogo genera un evento que permite a los consumidores actualizar sus propias proyecciones locales.

Esta estrategia elimina dependencias síncronas entre servicios y permite construir una arquitectura basada en eventos.

---

# Product Service como Fuente de Verdad

Product Service representa la única fuente oficial de información relacionada con productos.

Todos los cambios del catálogo se originan en este servicio.

```text
                Product Service
         (Single Source of Truth)
```

Los demás microservicios nunca modifican directamente la información del catálogo.

---

# Estrategia de Sincronización

Cada vez que ocurre un cambio relevante:

- creación de un producto
- actualización
- activación
- desactivación
- modificación del stock

Product Service publica un evento de dominio.

```text
             Product Service
                    │
                    ▼
           Persistir Cambio
                    │
                    ▼
         Publicar Domain Event
                    │
                    ▼
                RabbitMQ
                    │
        ┌───────────┴────────────┐
        ▼                        ▼
 Order Service           Future Services
```

Los consumidores actualizan sus propias proyecciones utilizando estos eventos.

---

# Modelo de Sincronización

El sistema implementa una estrategia basada en:

## Publicación de Eventos

Product Service comunica cada cambio del dominio.

No realiza llamadas REST para notificar actualizaciones.

---

## Consistencia Eventual

Los consumidores reciben los eventos y actualizan su información local.

Durante un corto período puede existir una diferencia entre:

- Product Service
- las proyecciones locales

Este comportamiento es esperado y aceptado dentro de una arquitectura Event-Driven.

---

# Eventos de Sincronización

Actualmente Product Service publica los siguientes eventos.

| Evento | Objetivo |
|---------|----------|
| PRODUCT_CREATED | Crear el producto en las proyecciones locales. |
| PRODUCT_UPDATED | Sincronizar información general del producto. |
| PRODUCT_ACTIVATED | Actualizar el estado a ACTIVE. |
| PRODUCT_DEACTIVATED | Actualizar el estado a INACTIVE. |
| PRODUCT_STOCK_UPDATED | Sincronizar el stock disponible. |

Cada evento representa un cambio ocurrido sobre el dominio.

---

# Consumidores

Actualmente los eventos son utilizados por:

- Order Service

En futuras versiones podrán ser consumidos por:

- Notification Service
- Inventory Service
- Search Service
- Recommendation Service
- Analytics Service

Product Service permanece completamente desacoplado de sus consumidores.

---

# Beneficios

La sincronización mediante eventos permite:

- eliminar llamadas REST entre microservicios
- reducir el acoplamiento
- mejorar la disponibilidad
- disminuir la latencia
- facilitar la escalabilidad
- permitir la incorporación de nuevos consumidores sin modificar Product Service

---

# Principios

## Single Source of Truth

Product Service continúa siendo el único responsable del catálogo.

---

## Publicador Desacoplado

Product Service publica eventos sin conocer quién los consume.

---

## Escalabilidad

Nuevos consumidores pueden incorporarse simplemente suscribiéndose a los eventos publicados.

No es necesario modificar Product Service.

---

## Evolución Independiente

Cada microservicio mantiene su propia proyección del catálogo.

Esto permite que evolucionen de forma independiente respetando los contratos de eventos compartidos.

---

# Evolución

Actualmente la sincronización se basa en la publicación de eventos mediante RabbitMQ.

En futuras iteraciones se incorporarán mecanismos adicionales para fortalecer la consistencia y resiliencia del sistema.

Entre ellos:

- Outbox Pattern
- Publisher Confirms
- Retry
- Dead Letter Queue (DLQ)
- Versionado de eventos
- Idempotencia
- Monitoreo de sincronización
- OpenTelemetry

Estas mejoras incrementarán la confiabilidad de la sincronización entre Product Service y los consumidores del ecosistema.