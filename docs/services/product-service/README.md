# Product Service

## Descripción

Product Service es el responsable de gestionar el catálogo de productos dentro del ecosistema **Ecommerce Microservices Demo**.

Es la fuente oficial de información (Single Source of Truth) para todos los datos relacionados con productos, incluyendo precios, disponibilidad y estado.

Además de administrar el catálogo, publica eventos de dominio que permiten mantener sincronizadas las proyecciones locales utilizadas por otros microservicios.

---

# Responsabilidades

Actualmente el servicio es responsable de:

- Gestionar el ciclo de vida de los productos.
- Administrar categorías.
- Gestionar precios.
- Gestionar stock.
- Activar y desactivar productos.
- Publicar eventos de dominio.
- Mantener la consistencia del catálogo.

---

# Arquitectura

Product Service representa el modelo operacional del sistema.

Todos los demás microservicios obtienen la información del catálogo mediante eventos publicados por este servicio.

```text
               Product Service

          (Single Source of Truth)

                    │

                    ▼

              RabbitMQ Events

        ┌────────────┴────────────┐

        ▼                         ▼

 Order Service         Notification Service

(Product Projection)
```

---

# Principios de Diseño

- Domain-Driven Design (DDD)
- Event-Driven Architecture (EDA)
- Single Source of Truth
- Publisher Pattern
- Bajo acoplamiento entre microservicios

---

# Funcionalidades

## Implementadas

- CRUD Productos.
- CRUD Categorías.
- Specification API.
- Paginación.
- Ordenamiento.
- Logging estructurado.
- RabbitMQ Producer.
- Eventos de dominio.
- JWT Security.

## Futuro

- Optimistic Locking.
- Auditoría.
- Versionado de eventos.
- Outbox Pattern.
- Publisher Confirms.

---

# Documentación

Para mayor información consultar:

- domain.md
- product-flow.md
- event-publication.md
- synchronization.md
- roadmap.md

---

# Tecnologías

- Java 21
- Spring Boot 3
- PostgreSQL
- Spring Data JPA
- RabbitMQ
- Spring Security
- JWT
- Log4j2

---

# Estado

✅ Servicio completamente funcional.

Actualmente Product Service representa la fuente oficial del catálogo y publica eventos que mantienen sincronizados los demás microservicios.