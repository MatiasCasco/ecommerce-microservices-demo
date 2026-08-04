# Future Architecture

## Objetivo

Este documento describe la visión de largo plazo para **Product Service**.

No representa funcionalidades implementadas.

Su propósito es documentar la evolución esperada del servicio a medida que el ecosistema crezca y la arquitectura madure.

---

# Visión

Product Service continuará siendo la **fuente oficial del catálogo de productos** dentro del ecosistema.

Su responsabilidad será mantener la integridad del catálogo y distribuir los cambios del dominio a los demás microservicios mediante eventos.

La evolución del servicio estará enfocada en:

- mayor resiliencia
- mayor observabilidad
- mayor escalabilidad
- mayor confiabilidad

---

# Arquitectura Objetivo

```text
                  Product Service
             (Single Source of Truth)
                        │
                        ▼
               Outbox Pattern
                        │
                        ▼
                  RabbitMQ
                        │
        ┌───────────────┼────────────────┐
        ▼               ▼                ▼
 Order Service   Inventory Service   Notification
        │               │                │
        ▼               ▼                ▼
 Local Projection  Stock Control     User Messages
```

La comunicación continuará siendo completamente desacoplada mediante eventos.

---

# Evolución de la Publicación de Eventos

Actualmente los eventos se publican directamente mediante RabbitMQ.

En futuras iteraciones la publicación evolucionará incorporando:

- Outbox Pattern
- Publisher Confirms
- Retry
- Dead Letter Queue (DLQ)
- Versionado de eventos

Objetivo:

Garantizar que ningún evento del dominio se pierda.

---

# Evolución del Catálogo

El modelo del catálogo podrá incorporar nuevas capacidades.

Posibles funcionalidades:

- múltiples imágenes
- atributos dinámicos
- variantes de productos
- múltiples monedas
- múltiples listas de precios
- promociones
- impuestos
- productos digitales
- productos físicos

El modelo fue diseñado para crecer sin afectar a los consumidores.

---

# Inventory Service

Actualmente Product Service administra el stock.

En futuras versiones esta responsabilidad podrá migrar a un servicio especializado.

```text
Hoy

Product
 ├── Precio
 ├── Estado
 └── Stock

↓

Futuro

Product Service
 ├── Precio
 ├── Estado

Inventory Service
 ├── Stock
 ├── Reservas
 ├── Movimientos
 └── Disponibilidad
```

Esta separación permitirá manejar escenarios complejos de inventario sin aumentar la complejidad del catálogo.

---

# Observabilidad

La observabilidad evolucionará incorporando:

- OpenTelemetry
- Distributed Tracing
- Métricas
- Dashboards
- Alertas
- Health Checks avanzados

Cada evento publicado podrá seguirse desde su origen hasta todos sus consumidores.

---

# Auditoría

El servicio incorporará mecanismos de auditoría para registrar:

- creación de productos
- modificaciones
- cambios de precio
- cambios de estado
- cambios de categoría

Esto permitirá reconstruir el historial completo del catálogo.

---

# Escalabilidad

La arquitectura permitirá:

- escalar múltiples instancias de Product Service
- incorporar nuevos consumidores sin modificar el productor
- aumentar el volumen de eventos publicados
- soportar un crecimiento continuo del catálogo

---

# Seguridad

La seguridad podrá evolucionar incorporando:

- OAuth2
- Client Credentials
- Rotación de claves
- Auditoría de accesos
- Rate Limiting

---

# Arquitectura Distribuida

La comunicación entre servicios continuará basada en eventos.

Nuevos consumidores podrán incorporarse sin modificar Product Service.

Ejemplos:

- Search Service
- Recommendation Service
- Analytics Service
- Pricing Service
- Inventory Service
- Notification Service

Esta arquitectura favorece la independencia entre equipos y la evolución de cada servicio.

---

# Principios que Permanecerán

Independientemente de la evolución del proyecto, Product Service mantendrá los siguientes principios:

- Product Service es la única fuente oficial del catálogo.
- El dominio continúa siendo el dueño de las reglas de negocio.
- Los cambios del catálogo se comunican mediante eventos.
- Los consumidores permanecen desacoplados.
- La arquitectura evoluciona sin romper contratos existentes.

---

# Estado Objetivo

La visión de Product Service es convertirse en un servicio altamente confiable, resiliente y escalable, capaz de soportar un ecosistema distribuido donde múltiples microservicios consuman eventos del catálogo sin depender directamente de llamadas síncronas.

La evolución del servicio se realizará de manera incremental, priorizando la simplicidad del diseño, la mantenibilidad y la estabilidad de los contratos de integración.